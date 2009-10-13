/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
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
