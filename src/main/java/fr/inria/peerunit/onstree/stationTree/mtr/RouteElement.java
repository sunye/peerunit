package fr.inria.peerunit.onstree.stationTree.mtr;

public class RouteElement
{
	private String ip=null;
	private int loss;
	public String getIp()
	{
		return ip;
	}
	
	public RouteElement(String ip, int loss)
	{
		this.ip=ip;
		this.loss=loss;
	}
	public void setIp(String ip)
	{
		this.ip = ip;
	}
	public int getLoss()
	{
		return loss;
	}
	public void setLoss(int loss)
	{
		this.loss = loss;
	}	
}
