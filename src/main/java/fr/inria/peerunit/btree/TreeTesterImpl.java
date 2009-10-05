package fr.inria.peerunit.btree;

import fr.inria.peerunit.GlobalVariables;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;

public class TreeTesterImpl extends AbstractTester implements Tester, Runnable {

    private boolean executing = true;
    private boolean isLastMethod = false;
    private ExecutorImpl executor;
    private TestCaseImpl testcase;

    private Verdicts v = Verdicts.PASS;
    private List<MethodDescription> testList;
    private static final Logger LOG = Logger.getLogger(TreeTesterImpl.class.getName());

    /**
     * Creates a new TreeTester with the specified id, and attached
     * to the specified Bootstrapper
     * @param id The tester's id
     * @param globals The tester's Bootstrapper
     */
    public TreeTesterImpl(int id, GlobalVariables gv) {
        super(gv);
        this.setId(id);
    }

    /**
     * Starts this tester
     * If the tester has been killed, it can't be started again
     */
    public void run() {
        LOG.log(Level.FINEST, "[TreeTesterImpl] start ");
        while (executing) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    LOG.log(Level.SEVERE, e.toString());
                }
            }
        }
    }

    public void execute(MethodDescription md) {
        LOG.log(Level.FINEST, "[TreeTesterImpl]  Tester " + getId() + " invoking");
        invoke(md);
    }

    /**
     * sets the test class for this tester
     * @param klass the test class to be processed by the tester
     */
    public void setClass(Class<? extends TestCaseImpl> klass) {
        executor = new ExecutorImpl(this, LOG);
        testList = executor.register(klass);
        newInstance(klass);
    }

    /**
     * @param c Test class
     * @throws IOException
     *
     * Creates the instances of peers and testers. Furthermore, creates the logfiles to them.
     */
    public void newInstance(Class<? extends TestCaseImpl> c) {

        try {

            testcase = (TestCaseImpl) c.newInstance();
            testcase.setTester(this);
        } catch (InstantiationException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        } catch (IllegalAccessException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        } catch (SecurityException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        } 
    }

    private void invoke(MethodDescription md) {
        assert executor != null : "Null executor";
        if (testList.contains(md)) {
            boolean error = true;
            try {
                Method m = executor.getMethod(md);
                m.invoke(testcase, (Object[]) null);
                error = false;
            } catch (IllegalArgumentException e) {
                for (StackTraceElement each : e.getStackTrace()) {
                    LOG.severe(each.toString());
                }

                v = Verdicts.INCONCLUSIVE;
            } catch (IllegalAccessException e) {
                for (StackTraceElement each : e.getStackTrace()) {
                    LOG.severe(each.toString());
                }

                v = Verdicts.INCONCLUSIVE;
            } catch (InvocationTargetException e) {
                Oracle oracle = new Oracle(e.getCause());
                if (oracle.isPeerUnitFailure()) {
                    error = false;
                }
                v = oracle.getVerdict();
                for (StackTraceElement each : e.getStackTrace()) {
                    LOG.severe(each.toString());
                }

            } finally {
                if (error) {
                    LOG.log(Level.WARNING, "[TreeTesterImpl]  Executed in " + md.getName());
                } else {
                    LOG.log(Level.INFO, "[TreeTesterImpl]  Executed " + md.getName());
                    if (executor.isLastMethod(md.getAnnotation())) {
                        LOG.log(Level.FINEST, "[TreeTesterImpl] Test Case finished by annotation " + md.getAnnotation());
                        LOG.log(Level.FINEST, "Local verdict " + v);
                        isLastMethod = true;
                    }
                }
            }
        }
    }

    /**
     * Returns this tester's verdict
     * @return the tester's verdict
     */
    public Verdicts getVerdict() {
        return v;
    }

    /**
     * Determines if the last method has been invoked
     * @return true if the last method has been invoked
     */
    public boolean isLastMethod() {
        return isLastMethod;
    }



    /**
     * Kills the tester, preventing it from processing any other treatment
     */
    public void kill() {
        executing = false;
        synchronized (this) {
            this.notify();
        }
    }

	public void setCoordinator(Coordinator coord) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
