package fr.inria.peerunit;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;

@SuppressWarnings("restriction")
public class LowerTest {
    private String className;
    private String fieldName;
    private int ltPort; 
    
	protected static Logger LOG = Logger.getLogger(LowerTest.class.getName());
	   /*
     *  LOWER TESTER
     */
	
	public LowerTest(String className, String fieldName, int port) {
		this.className = className;
		this.fieldName = fieldName;
		ltPort = port;
	}
	
    public void lowerTester() throws InterruptedException, RemoteException, IOException {


        LOG.info("Starting lower tester...");


        VMAcquirer vma = new VMAcquirer();

        LOG.info("Trying to connect at remote JPDA server on port " + ltPort);
        vma.connect(ltPort);
        LOG.info("Debugger connected!");


        LOG.info("Class: " + className + " e Field: " + fieldName);

        VirtualMachine vm = vma.getVM();
        LOG.info("aaaa" + vm.name());
        List<ReferenceType> referenceTypes = vm.classesByName(className);

        // Select fields
        for (ReferenceType refType : referenceTypes) {
            addFieldWatch(vm, refType);
        }

        // watch for loaded classes
        addClassWatch(vm);

        LOG.info("Class " + className + " has been watching!");

        // resume the vm
        vm.resume();

        // process events
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            EventSet eventSet = eventQueue.remove();
            for (Event event : eventSet) {
                if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                    // exit
                    return;
                } else if (event instanceof ClassPrepareEvent) {
                    // watch field on loaded class
                    ClassPrepareEvent classPrepEvent = (ClassPrepareEvent) event;
                    ReferenceType refType = classPrepEvent.referenceType();
                    addFieldWatch(vm, refType);
                } else if (event instanceof ModificationWatchpointEvent) {
                    // a Test.foo has changed
                    ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;
                    System.out.println("old=" + modEvent.valueCurrent());
                    System.out.println("new=" + modEvent.valueToBe());
                    System.out.println();
                    String num = modEvent.valueToBe().toString();

                    System.out.println("hahahahahaha");

                    int comp = Integer.valueOf(num);

                    if (comp == 10) {
                        System.out.println("Suspending execution");
                        vm.suspend();
                    }
                }
            }
            eventSet.resume();
        }
    }

    private  void addClassWatch(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.addClassFilter(className);
        classPrepareRequest.setEnabled(true);
    }

    private  void addFieldWatch(VirtualMachine vm,
            ReferenceType refType) {
        EventRequestManager erm = vm.eventRequestManager();
        Field field = refType.fieldByName(fieldName);
        ModificationWatchpointRequest modificationWatchpointRequest = erm.createModificationWatchpointRequest(field);
        modificationWatchpointRequest.setEnabled(true);
    }
}
