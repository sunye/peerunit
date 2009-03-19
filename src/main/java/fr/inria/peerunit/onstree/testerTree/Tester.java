package fr.inria.peerunit.onstree.testerTree;

/**
 * @author Jeremy
 *
 */
public class Tester 
{
	private Integer num;
	private String action;
	
	public Tester(Integer num, String action) 
	{
		super();
		this.num = num;
		this.action = action;
	}
	
	public Integer getNum() 
	{
		return num;
	}
	
	public void setNum(Integer num) 
	{
		this.num = num;
	}
	
	public String getAction() 
	{
		return action;
	}
	
	public void setAction(String action) 
	{
		this.action = action;
	}

	

}
