package fr.inria.peerunit.rmi.coord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* @author Eduardo Almeida.
* @version 1.0
* @since 1.0
* @see ExecutionTime
* @see java.lang.System#currentTimeMillis()
*/
public class Chronometer {
	
	/**
	* @see getExecutionTime()
	*/
	private Map<String, ExecutionTime> executionTable= new HashMap<String, ExecutionTime>();
		
	/**
	* Associates an ExecutionTime (the current time in milliseconds) with the specified key in the map executionTable
	* @param method key with which a value will be associated
	* @see java.util.Map#put(Object, Object)
	*/
	public void start(String method){						
		executionTable.put(method, new ExecutionTime(System.currentTimeMillis()));
	}
	
	/**
	* Replaces the value at the specified key in the map executionTable by the current time in milliseconds minus the old value
	*
	* @param method key whose her value will be updated
	* @see java.util.Map#get(Object)
	* @see ExecutionTime#update(Long)
	*/
	public void stop(String method){
		executionTable.get(method).update(System.currentTimeMillis());
	}
	
	/**
	* Returns the time at the specified key. An index ranges from 0 to length()-1.
	*
	* @param method key of the desired time.
	* @return the desired time.
	* @see ExecutionTime#getTime()
	* @see java.util.Map#get(Object)
	*/
	public long getTime(String method){
		return executionTable.get(method).getTime();
	}	
	
	/**
	* Returns the character at the specified index. An index ranges from 0 to length()-1.
	*
	* @return a collection of the map's elements executionTable
	* @see java.util.Map#entrySet()
	*/
	public Set<Map.Entry<String, ExecutionTime>> getExecutionTime(){
		return executionTable.entrySet();
	}	
}
