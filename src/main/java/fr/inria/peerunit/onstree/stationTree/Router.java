package fr.inria.peerunit.onstree.stationTree;

import java.util.ArrayList;
import java.util.List;

public class Router
{
	private String ip;
	private List<Station> listStation;
	private List<Router> listRouter;

	public Router(String ip, List<Station> listStation)
	{
		super();
		this.ip = ip;
		this.listStation = listStation;
		listRouter = new ArrayList<Router>();
	}

	public Router(String ip)
	{
		super();
		this.ip = ip;
		listRouter = new ArrayList<Router>();
		listStation = new ArrayList<Station>();
	}

	public boolean containsRouter(String ipRouter)
	{
		for(Router rt:listRouter)
		{
			if(rt.getIp().equals(ipRouter))
			{
					return true;
			}			
		}
		return false;
	}
	
	public Router addRouter(String ipRouter)
	{
		Router rt = new Router(ipRouter);
		listRouter.add(rt);
		return rt;
	}
	
	public Router getRouter(String ipRouter)
	{
		for(Router rt:listRouter)
		{
			if(rt.getIp().equals(ipRouter))
			{
					return rt;
			}			
		}
		return null;
	}
	
	private boolean containsStation(String ipStation)
	{
		for(Station st:listStation)
		{
			if(st.getIp().equals(ipStation))
			{
				return true;
			}
		}
		return false;
	}
	
	public void addStation(Station st)
	{
		if(!containsStation(st.getIp()))
		{
			listStation.add(st);
		}
	}
	
	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public List<Station> getListStation()
	{
		return listStation;
	}

	public void setListStation(List<Station> listStation)
	{
		this.listStation = listStation;
	}

	public List<Router> getListRooter()
	{
		return listRouter;
	}

	public void setListRooter(List<Router> listRooter)
	{
		this.listRouter = listRooter;
	}

	public void print()
	{
		System.out.println("|");
		System.out.println("| -- Router : "+this.ip);
		System.out.println("|");
	}

	public String graphicPrintRouter()
	{		
		StringBuffer subTree= 
			new StringBuffer("\t"+"Router [shape=box, style=filled]; "+
				getIp().replaceAll("\\.","")+";");
		for (Router router :listRouter)
		{
			subTree.append(router.graphicPrintRouter());
			
			subTree.append("\t"+getIp().replaceAll("\\.","")+" -> "+
					router.getIp().replaceAll("\\.","")+";");			
		}
		for (Station station :listStation)
		{
			subTree.append(station.graphicPrintRouter());
			subTree.append("\t"+getIp().replaceAll("\\.","")+" -> "+
					station.getName()+";");						
		}
		return subTree.toString();
	}
		
}
