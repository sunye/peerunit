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
