/**
 * 
 */
package fr.inria.peerunit.onstree.stationTree;

import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.util.Util;

/** Use for station tree, it contains one station and stations children
 * @author Jeremy
 * @author Aboubakar Koïta 
 */
public class Node implements StationContainer
{
	private String num;	
	private Station stParent;
	private List<Node> listChildStation;
	
	public Node()
	{
		num = "null";
		listChildStation = new ArrayList<Node>();
	}
	
	public Node(String num, Station stParent)
	{
		super();
		this.num = num;
		this.stParent = stParent;
		listChildStation = new ArrayList<Node>();
	}
	
	public Node(Node firstNodeChild)
	{
		this.num = firstNodeChild.getNum();
		this.stParent = firstNodeChild.getStParent();
		listChildStation = firstNodeChild.listChildStation;
	}

	private boolean containsStation(String ipStation)
	{
		for(Node n:listChildStation)
		{
			if(n.getStParent().getIp().equals(ipStation))
			{
				return true;
			}
		}
		return false;
	}
	
	public Node addStation(Station st)
	{
		if(!containsStation(st.getIp()))
		{
			Node nChild = new Node(num+"."+(listChildStation.size()+1),st);
			listChildStation.add(nChild);
			return nChild;
		}
		return null;
	}

	public Station getStParent()
	{
		return stParent;
	}
	
	public String getNum()
	{
		return num;
	}
	
	public int getChildrenNumber()
	{
		int length = listChildStation.size();
		
		for(Node n:listChildStation)
		{
			length+=n.getChildrenNumber();
		}
		
		return  length;
	}
	
	public void print()
	{
		if(TesterUtil.instance.getStationTreeTrace() == 1)
		{
			System.out.println("|");
			System.out.println("|-Station: "+num+" name: "+stParent.getName());
			for(Node n:listChildStation)
			{
				n.print();
			}
		}
	}
	
	public String graphicPrint()
	{
		String sColor = Util.getColor();
		StringBuffer subTree = new StringBuffer("\t"+"node [shape=circle, style=filled, color="+sColor+"]; "+getStParent().getName()+";");
		for (Node node : listChildStation)
		{
			subTree.append(node.graphicPrint());			
			if(Util.areInSameNet(stParent.getIp(),node.getStParent().getIp()))
			{
				subTree.append("\t"+getStParent().getName()+" -> "+
						node.getStParent().getName()+";");							
			}
			else
			{
				subTree.append("\t"+getStParent().getName()+" -> "+
						node.getStParent().getName()+"[color="+sColor+"];");							
			}
			
		}
		return subTree.toString();
	}

	public List<Node> getListChildStation()
	{
		return listChildStation;
	}

	public StationRoot getStRoot()
	{
		return null;
	}

	public Station getStation() {  
		return stParent;
	}
	
}
