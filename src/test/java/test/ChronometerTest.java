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
package test;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.rmi.coord.Chronometer;
import fr.inria.peerunit.rmi.coord.ExecutionTime;

public class ChronometerTest {
	
	private static Chronometer chrono ;
	private static String method = "test";
	private static String method2 = "test2";
	private static String method3 = "test3";
	static long tp1 ;
	static long tp2 ;
	static long tp3 ;
	static long tp4 ;
	
	@BeforeClass
	public static void  inititalize() {
		chrono = new Chronometer();

		chrono.start(method3);
		tp1 = chrono.getTime(method3);
		
		chrono.start(method);
		chrono.stop(method);
		tp3 = chrono.getTime(method);
		chrono.start(method);
		tp4 = chrono.getTime(method);
		
		chrono.start(method3);
		tp2 = chrono.getTime(method3);
		chrono.start(method3);

		//chrono.stop(method4);
		
	}

	public void setup(){
		inititalize();
		chronoTest() ;
	}
	
	@Test
	public void chronoTest() {
		assertNotNull(chrono.getTime(method));
		assertFalse(chrono.getExecutionTime().contains(method2));
		//assertNull(chrono.getTime(method2));
		//assertNotNull(chrono.getTime(method4));
		assertFalse(chrono.getExecutionTime().contains(method2));
		int result = 0;
		for (Entry<String, ExecutionTime> i : chrono.getExecutionTime()) {
			if ((i.getKey()== method3)){result ++;	}
		}
		
		assertEquals(1,result);
		assertEquals(tp1,tp2);
		assertTrue(tp3 < tp4);
		
	}
	
}
