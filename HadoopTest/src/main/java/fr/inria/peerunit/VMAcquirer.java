package fr.inria.peerunit;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import java.io.IOException;
import java.util.Map;

public class VMAcquirer {

  static VirtualMachine vm;
  static AttachingConnector connector;

  /**
   * Call this with the localhost port to connect to.
   */
  public void connect(int port) throws IOException, InterruptedException {
    
    final String strPort = Integer.toString(port);

    final Thread thread = new Thread() {

        @Override public void run() {
               try {

    			VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();

    			for (Connector con : vmManager.attachingConnectors()) {
      				if ("com.sun.jdi.SocketAttach".equals(connector.name())) {
        				System.out.println(connector.name());
        				connector = (AttachingConnector) con;
      				}
    			}
	
                   	Map<String, Connector.Argument> args = connector.defaultArguments();
                   	
			System.out.println("Port: "+args.get("port"));

			Connector.Argument pidArgument = args.get("port");

                    	if (pidArgument == null) {
                        	throw new IllegalStateException();
                     	}

                    	pidArgument.setValue(strPort);

                    	vm = connector.attach(args);

               } catch (Exception e) {

               }
           }
     };

  }

  public VirtualMachine getVM() {
        return vm;
  }

}
