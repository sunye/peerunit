package fr.inria.peerunit.onstree.stationTree;

import java.util.ArrayList;
import java.util.List;
import fr.inria.peerunit.util.Util;

public class StationRoot
{
	private String ip;
	private String name;
	private List<Router> listRouter;
	private List<Station> listStation;
	
	public StationRoot(String ip, String name) 
	{
		super();
		this.ip = ip;
		this.name = name;
		listStation = new ArrayList<Station>();
		listRouter = new ArrayList<Router>();
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
	
	
	public boolean containsStation(String ipStation)
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
	
	public List<Router> getListRouter() 
	{
		return listRouter;
	}

	public String getIp() 
	{
		return ip;
	}

	public void setIp(String ip) 
	{
		this.ip = ip;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public List<Station> getListStation()
	{
		return listStation;
	}	
	
	public String graphicPrint()
	{
		String sColor = Util.getColor();
		return "\t"+"node [shape=circle, style=filled, color="+sColor+"]; "+getName()+";";
	}
	
	public String graphicPrintRouters(){
		StringBuffer tree=new StringBuffer("\t"+"node [shape=box, style=filled]; "+getName()+";");
		for (Router router : listRouter)
		{

			tree.append(router.graphicPrintRouter());
			tree.append("\t"+getName()+" -> "+
						router.getIp().replaceAll("\\.","")+";");				
		}
		
		for (Station station :listStation)
		{
			tree.append(station.graphicPrintRouter());
			tree.append("\t"+getName()+" -> "+
					station.getName()+";");			
			
		}		
		return tree.toString();
	}

}
