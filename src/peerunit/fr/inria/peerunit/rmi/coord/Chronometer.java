package fr.inria.peerunit.rmi.coord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Chronometer {
	
	private Map<String, Long> executionTable= new HashMap<String, Long>();
		
	public void start(String method){		
		executionTable.put(method, System.currentTimeMillis());				
	}
	
	public void stop(String method){
		Long temp= executionTable.get(method);
		executionTable.remove(method);
		executionTable.put(method, System.currentTimeMillis()-temp.longValue());
	}
	
	public long getTime(String method){
		return executionTable.get(method);
	}	
	
	public Set<Map.Entry<String, Long>> getExecutionTime(){
		return executionTable.entrySet();
	}	
}
