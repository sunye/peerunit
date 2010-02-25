package com.alma.rmilite.server;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;

import com.alma.rmilite.RemoteMethodResult;
import com.alma.rmilite.RemoteMethod;
import com.alma.rmilite.UnexportedException;

public class SkeletonImpl implements Skeleton {
	
	private class RemoteMethodCall implements Runnable {
		
		private Socket client;
		
		public RemoteMethodCall(Socket client) {
			this.client = client;
		}
		
		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(this.client.getInputStream());
				RemoteMethod remoteMethod = (RemoteMethod) in.readUnshared();
				
				RemoteMethodResult remoteMethodResult = new RemoteMethodResult(remoteMethod.getMethod());
				try {
					remoteMethodResult.setResult(SkeletonImpl.this.execute(remoteMethod), false);
				} catch (UnexportedException t) {
					t.printStackTrace();
				} catch (NotSerializableException t) {
					t.printStackTrace();
				} catch (Throwable t) {
					remoteMethodResult.setResult(t, true);
				}
				
				ObjectOutputStream out = new ObjectOutputStream(this.client.getOutputStream());
				out.writeUnshared(remoteMethodResult);
				out.flush();

				this.client.close();
			} catch (IOException i) {
				i.printStackTrace();
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
			} catch (UnexportedException u) {
				u.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Remote object;
	private ServerSocket server;
	
	public SkeletonImpl(int port, Remote object) throws IOException {
		this.object = object;
		this.server = new ServerSocket(port);
		
		new Thread(new Runnable() {
			public void run() {
				while (!server.isClosed()) {
					try {
						new Thread(new RemoteMethodCall(server.accept())).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}
		}).start();
	}
	
	public SkeletonImpl(Remote object) throws IOException {
		this(0, object);
	}
	
	synchronized private Object execute(RemoteMethod remoteMethod) throws Throwable {
		return remoteMethod.getMethod().invoke(this.object, remoteMethod.getArgs());
	}
	
	@Override
	public boolean close() throws IOException {
		server.close();
		return true;
	}

	@Override
	public int getPort() {
		return this.server.getLocalPort();
	}
}
