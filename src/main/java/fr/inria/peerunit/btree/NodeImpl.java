package fr.inria.peerunit.btree;

import fr.inria.peerunit.GlobalVariables;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.util.BTreeNode;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta
 * @version 1.0
 * @since 1.0
 */
public class NodeImpl implements Node, Serializable, Runnable {
    /*
     * @TODO: It seems that this class has too many attributes.
     */


    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(NodeImpl.class.getName());

    private List<TreeTesterImpl> testers = new Vector<TreeTesterImpl>();
    private List<MethodDescription> testList = new ArrayList<MethodDescription>();

    /**
     * The Bootstraper node.
     */
    final private Bootstrapper boot;
    final private GlobalVariables globals;

    private int id;
    private boolean amIRoot = false;
    private boolean amILeaf = false;
    private boolean isLastMethod = false;
    private int numberOfChildren = 0;
    private TreeElements tree = new TreeElements();
    private BTreeNode bt;
    private AtomicInteger childrenTalk = new AtomicInteger(0);
    private MethodDescription mdToExecute;
    private int treeWaitForMethod = TesterUtil.instance.getTreeWaitForMethod();
    private Class<? extends TestCaseImpl> testCaseClass;
    private List<Verdicts> localVerdicts = new Vector<Verdicts>();
    private String hostAddress = null;

    /**
     * Constructs a new Node, and registers it to the specified Bootstrapper
     * If the Bootstrapper already has reached it's max number of nodes,
     * the system exits
     * @param b
     * @throws java.rmi.RemoteException
     */
    public NodeImpl(Bootstrapper b, GlobalVariables gv) throws RemoteException {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            this.hostAddress = addr.getHostAddress();
        } catch (UnknownHostException ex) {
            log.log(Level.SEVERE, null, ex);
        } 

        boot = b;
        globals = gv;
        UnicastRemoteObject.exportObject(this);
        id = boot.register(this);
        amIRoot = boot.isRoot(id);
    }

    /**
     * Retrieves all the test methods to be executed by this node
     * @param c The test class
     */
    public void registerTestCase(Class<? extends TestCaseImpl> c) {
        ExecutorImpl executor;
        try {
            log.log(Level.INFO, "[NodeImpl] Registering actions");
            executor = new ExecutorImpl(null, log);
            testList = executor.register(c);
            testCaseClass = c;
        } catch (SecurityException e) {
            log.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * Runs the Node. The node will wait for the tree construction to be complete,
     * then executes the test methods, and generates and logs a verdict for these tests.
     * When it's finished, it exits the System
     */
    public void run() {
        long time;
        /**
         * Now starting the Testers
         */
        startTesters();
        if (amIRoot) {
            try {
                Thread.sleep(TesterUtil.instance.getWaitForMethod());
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, e.toString());
            }
        }
        time = System.currentTimeMillis();
        log.log(Level.FINEST, "[NodeImpl] START EXECUTION ");
        for (MethodDescription md : testList) {
            mdToExecute = md;
            log.log(Level.FINEST, "[NodeImpl] METHOD " + mdToExecute);
            try {
                if (amIRoot) {
                    log.log(Level.FINEST, "[NodeImpl] Start action ");
                    log.log(Level.FINEST, "[NodeImpl] dispatch(); IamRoot, id:" + id);
                    dispatch();
                } else {
                    /**
                     * Wait for parent
                     */
                    log.log(Level.FINEST, "[NodeImpl] Wait for parent");
                    synchronized (this) {
                        this.wait();
                    }
                    log.log(Level.FINEST, "[NodeImpl] Stop Wait for parent");
                    log.log(Level.FINEST, "[NodeImpl] I'm about to execute " + md);
                    if (!amILeaf) {
                        log.log(Level.FINEST, "[NodeImpl] dispatch() !amILeaf, id:" + id);
                        dispatch();
                    } else {
                        log.log(Level.FINEST, "[NodeImpl] execute(); IamLeaf, id:" + id);
                        execute();
                    }
                    log.log(Level.FINEST, "[NodeImpl] talkToParent");
                    talkToParent();
                }
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, e.toString());
            }
        }
        log.log(Level.INFO, "Whole execution time " + (System.currentTimeMillis() - time));
        if (amIRoot) {
            GlobalVerdict verdict = new GlobalVerdict(TesterUtil.instance.getRelaxIndex());
            for (Verdicts v : localVerdicts) {
                verdict.addLocalVerdict(v);
            }
            log.log(Level.INFO, "Final verdict " + verdict);
        }
        System.exit(0);
    }

    private void dispatch() throws InterruptedException {
        log.log(Level.INFO, id + "[NodeImpl] Dispatching action " + mdToExecute);
        log.log(Level.FINEST, "[NodeImpl] talkToChildren()");
        dispatchMessageToChildren();
        log.log(Level.FINEST, "[NodeImpl] execute()");
        execute();
        /**
         * Wait for children
         */
        log.log(Level.FINEST, "[NodeImpl] Wait for children");
        synchronized (this) {
            this.wait();
        }
        log.log(Level.FINEST, "[NodeImpl] Stop wait");
    }

    private void execute() {
        for (TreeTesterImpl t : testers) {
            log.log(Level.INFO, id + "[NodeImpl] Tester " + t.getId() + " Executing action " + mdToExecute);
            synchronized (t) {
                t.execute(mdToExecute);
            }
            if (t.isLastMethod()) {
                isLastMethod = t.isLastMethod();
                localVerdicts.add(t.getVerdict());
            }
        }
    }

    private void dispatchMessageToChildren() {
        for (Node child : tree.getChildren()) {
            log.log(Level.FINEST, id + "[NodeImpl] talk to kids " + child);
            log.log(Level.FINEST, id + "[NodeImpl] Sending them " + mdToExecute);
            try {
                /**
                 * Talk to children
                 */
                child.accept(MessageType.EXECUTE, mdToExecute);
            } catch (RemoteException e) {
                log.log(Level.SEVERE, e.toString());
            }
        }
    }

    private void talkToParent() {
        log.log(Level.FINEST, id + "[NodeImpl] talk do daddy");
        try {
            /**
             * Talk to parent
             */
            Thread.sleep(treeWaitForMethod);
            if (isLastMethod) {
                tree.getParent().acceptVerdict(localVerdicts);
            }
            tree.getParent().accept(MessageType.OK, mdToExecute);
        } catch (RemoteException e) {
            log.log(Level.SEVERE, e.toString());
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * 
     * @param message
     * @param mdToExecute
     * @throws RemoteException
     */
    public void accept(MessageType message, MethodDescription mdToExecute) throws RemoteException {
        log.log(Level.FINEST, id + "[NodeImpl] Daddy asked me to execute " + mdToExecute);
        this.mdToExecute = mdToExecute;
        /**
         * Way up
         */
        int talked;
        if (message.equals(MessageType.OK)) {
            talked = childrenTalk.incrementAndGet();
            log.log(Level.FINEST, id + "[NodeImpl]  I finished the execution. Waiting " +
                    ((numberOfChildren - talked) + 1) + " of my " + numberOfChildren + " children ");

            /**
             * I have to wait for my children
             */
            if (talked == numberOfChildren) {
                synchronized (this) {
                    this.notify();
                }
                childrenTalk.set(0);
            }


            /**
             * now EXECUTE messages
             */
        } else {
            /**
             * Way down
             */
            if (message.equals(MessageType.EXECUTE)) {
                log.log(Level.FINEST, id + "[NodeImpl]  I'm about to execute.");
                synchronized (this) {
                    this.notify();
                }
            }
        }
    }

    public void acceptVerdict(List<Verdicts> localVerdicts) throws RemoteException {
            this.localVerdicts.addAll(localVerdicts);
    }

    public void setElements(BTreeNode bt, TreeElements tree) throws RemoteException {
        log.log(Level.FINEST, "[NodeImpl] id " + id + " bt " + bt + " tree " + tree);
        this.tree = tree;
        this.bt = bt;
        for (BTreeNode child : this.bt.getChildren()) {
            if (child != null) {
                numberOfChildren++;
            }
        }
        log.log(Level.FINEST, "[NodeImpl] I have these number of children: " + numberOfChildren);
        bt.getKeys();
        amILeaf = bt.isLeaf();
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * Returns this node's id
     * @return the node's id
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Node id: " + id;
    }

    @SuppressWarnings("unchecked")
	private synchronized void startTesters() {
        /**
         * Initially we wait for the tree construction
         */
        try {
            this.wait();
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, e.toString());
        }
        System.out.println("Réveil");
        log.log(Level.INFO, "[NodeImpl] Starting " + bt.getKeys() + " Testers ");
        /**
         * Using bt Node acknowledge the testers it must control, then start them
         */
        System.out.println("Début création des testeurs");
        for (Comparable key : bt.getKeys()) {
            if (key != null) {
                int peerID = new Integer(key.toString());
                log.log(Level.FINEST, "[NodeImpl] Tester " + key.toString());
                testers.add(new TreeTesterImpl(peerID, globals));
            }
        }

        System.out.println("Testers créés");
        /**
         * Let's start testers
         */
        for (TreeTesterImpl t : testers) {
            log.log(Level.FINEST, "[NodeImpl] Starting Tester " + t);
            t.setClass(testCaseClass);
            new Thread(t).start();
        }
        System.out.println("Testeurs démarrés");
        log.log(Level.FINEST, "[NodeImpl] Testers added: " + testers.size());
    }

    public String getIP() {
        return hostAddress;
    }
}
