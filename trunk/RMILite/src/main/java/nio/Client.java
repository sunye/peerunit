package nio;

import java.io.Serializable;

/** 
 * A client can send {@link Serializable} data to a distant server<br />
 * It is used as a 
 * @author E06A193P
 *
 */
public interface Client {
	
	public boolean send(Serializable s);
	
	public void close();

}
