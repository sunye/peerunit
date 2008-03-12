package fr.inria.peerunit.rmi.coord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Chronometer {
	
	private Map<String, ExecutionTime> executionTable= new HashMap<String, ExecutionTime>();
		
	public void start(String method){						
		executionTable.put(method, new ExecutionTime(System.currentTimeMillis()));
	}
	
	public void stop(String method){
		executionTable.get(method).update(System.currentTimeMillis());
	}
	
	public long getTime(String method){
		return executionTable.get(method).getTime();
	}	
	
	public Set<Map.Entry<String, ExecutionTime>> getExecutionTime(){
		return executionTable.entrySet();
	}	
}
