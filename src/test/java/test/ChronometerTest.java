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
	private static String method4 = "test4";
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
