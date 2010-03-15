package nio;

import java.io.Serializable;

/**
 * A client can send {@link Serializable} data to a distant server<br />
 * It is used as a
 * @author E06A193P
 */
public interface Writer {

	/**
	 * Send an object to the server
	 * @param s
	 *          the {@link Serializable} to send
	 * @return true if s was sent correctly
	 */
	public boolean send( Serializable s );

	/**
	 * close the socket associated. No more data can then be sent/received
	 */
	public void close();

}
