package fr.inria.peerunit.rmi.coord;

public class ExecutionTime {
	private Long time;
	
	public ExecutionTime(Long t){
		this.time=t;
	}
	
	public void update(Long t){
		this.time=t-this.time;
	}
	
	public Long getTime(){
		return this.time;
	}
}
