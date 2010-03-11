package nio;

public interface Server extends Runnable {

	/**
	 * Set the number of threads this server should use to process incoming data
	 */
	public void setNbThreads( int nbThreads );

	/** @return the number of threads this server uses to handle incoming data */
	public int getNbThreads();

	/** ask the server to stop as soon as possible */
	public void stop();

	/** is the server listening to the socket? */
	public boolean isRunning();

}
