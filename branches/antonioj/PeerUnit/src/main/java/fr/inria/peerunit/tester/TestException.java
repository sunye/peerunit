package fr.inria.peerunit.tester;


public abstract class TestException extends AssertionError {

    private static final long serialVersionUID = 1L;

    /**
     * @param message Exception message.
     */
    TestException(String message) {
        super(message);
    }
}