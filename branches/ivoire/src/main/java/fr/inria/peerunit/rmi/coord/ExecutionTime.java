package fr.inria.peerunit.rmi.coord;

/**
* @author Eduardo Almeida.
* @version 1.0
* @since 1.0 
* @see java.lang.Long#toString()
*/
public class ExecutionTime {
	/**
	* @see update(Long t)
	*/
	private Long time;

	/**
	 * 
	 * @param t time of the ExecutionTime which will be created
	 */
	public ExecutionTime(Long t){
		this.time=t;
	}

	/**
	* Replaces the time by the specified time  minus the old time
	*
	* @param t use to recalculate the new time
	*/
	public void update(Long t){
		this.time=t-this.time;
	}

	/**
	 * @return the time
	 */
	public Long getTime(){
		return this.time;
	}
	
	/**
	 * @return Returns a String representing the time on milliseconds with the diminutive msec 
	 */
	public String toString() {
		return time.toString()+  " msec.";
	}
}
