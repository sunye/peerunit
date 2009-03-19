package fr.inria.peerunit.onstree.stationTree;

import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.util.TesterUtil;

/**
 * @author Jeremy
 * 
 */
public class Station
{
	private Integer nbSaut;
	private ArrayList<String> listSaut;
	private String ip;
	private String name;

	public Station(String slistSaut, String ip, String name)
	{
		super();
		this.listSaut = new ArrayList<String>();
		if(slistSaut.contains("."))
		{
			if(slistSaut.contains("/"))
			{
				String[] tab_slistSaut = slistSaut.split("/");
				for(String sSaut:tab_slistSaut)
				{
					listSaut.add(sSaut);
				}
				this.nbSaut = listSaut.size();
			}
			else
			{
				listSaut.add(slistSaut);
				this.nbSaut = 1;
			}
		}
		else
		{
			this.nbSaut = 0;
		}
		this.ip = ip;
		this.name = name;
		print();
	}

	public Integer getNbSaut()
	{
		return nbSaut;
	}

	public void setNbSaut(Integer nbSaut)
	{
		this.nbSaut = nbSaut;
	}

	public ArrayList<String> getListSaut()
	{
		return listSaut;
	}

	public void setListSaut(ArrayList<String> listSaut)
	{
		this.listSaut = listSaut;
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
	
	public void print()
	{
		if(TesterUtil.getStationTreeTrace() == 1)
		{
			System.out.println("____________________");
			System.out.println("|   "+this.ip+"   | -- Station "+this.name+" saut :"+nbSaut);
			System.out.println("____________________");
		}
	}

	public String graphicPrintRouter()
	{
		return
		"\t"+"Station [shape=circle, style=filled]; "+
		getName()+"[color=red];";
	}

}
