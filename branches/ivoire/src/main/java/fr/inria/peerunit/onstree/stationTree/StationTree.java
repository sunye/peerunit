package fr.inria.peerunit.onstree.stationTree;

import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.util.Util;

/*
 * @author Jeremy
 * @author Aboubakar Koïta  
 */
public class StationTree  implements StationContainer  //XXX
{
	private String num;	
	private StationRoot stRoot;
	private List<Node> listChildStation;
	
	public StationTree(String num, StationRoot stRoot)
	{
		super();
		this.num = num;
		this.stRoot = stRoot;
		listChildStation = new ArrayList<Node>();
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
	
	public List<Node> getListChildStation()
	{
		return listChildStation;
	}

	public StationRoot getStRoot()
	{
		return stRoot;
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
			System.out.println("|-StationRoot: "+num+" name: "+stRoot.getName());
			for(Node n:listChildStation)
			{
				n.print();
			}
		}
	}
	
	public String graphicPrint()
	{
		Util.initColor();
		StringBuffer tree =new StringBuffer(stRoot.graphicPrint());
		for (Node node : listChildStation)
		{
			tree.append(node.graphicPrint());
			if(!stRoot.containsStation(node.getStParent().getIp()))
			{
				tree.append("\t"+stRoot.getName()+" -> "+
					node.getStParent().getName()+"[color=green];");
			}
			else
			{
				tree.append("\t"+stRoot.getName()+" -> "+
						node.getStParent().getName()+";");				
			}
		}
		return tree.toString();
	}

	public AbstractStation getStation() {
		return stRoot;
	}
	
}
