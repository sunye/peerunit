package nio;

import java.io.Serializable;


public interface Client {
	
	public boolean send(Serializable s);
	
	public void close();

}
