package fr.inria.peerunit.onstree.stationTree;

import java.util.List;

import fr.inria.peerunit.util.TesterUtil;

public class StationTreeBuilder 
{

	public void buildNetTree(List<Station> listStation, StationRoot stRoot)
	{
		String currentNode = "";
		Router currentRouter = new Router("localhost");
		Router newRouter = new Router("localhost");
		int numNode = 0;
		
		if(TesterUtil.instance.getStationTreeTrace() == 1)
		{
			System.out.println("____________________");
			System.out.println("|   "+stRoot.getName()+"   |");
			System.out.println("____________________");
		}
		
		for(Station st:listStation)
		{
			// if station contains some router 
			if(st.getNbSaut()>0)
			{
				for(numNode = 0; numNode<st.getListSaut().size(); numNode++)
				{
					
					currentNode = st.getListSaut().get(numNode);
					if(currentNode != "" && numNode == 0)					
					{
						if(!stRoot.containsRouter(currentNode))
						{
							currentRouter = stRoot.addRouter(currentNode);
							currentRouter.print();
						}
						else
						{
							// return the router following
							currentRouter = stRoot.getRouter(currentNode);
							currentRouter.print();
						}
					}
					else if(currentNode != "")
					{
						if(!currentRouter.containsRouter(currentNode))
						{
							newRouter = currentRouter.addRouter(currentNode);
							newRouter.print();
							currentRouter = newRouter;
						}
						else
						{
							// return the router following
							currentRouter = currentRouter.getRouter(currentNode);
							currentRouter.print();
						}
						
					}
				}
				// add the station to Router
				currentRouter.addStation(st);
				st.print();
			}
			else
			{
				stRoot.addStation(st);
				st.print();
			}
		}
	}

	public StationTree buildStationTree(StationRoot stRoot)
	{
		StationTree stTree = new StationTree("1",stRoot);		
				
		Node nodeChild = new Node();
		Node firstNodeChild = new Node();
		for(Station st1:stRoot.getListStation())
		{			
			firstNodeChild = stTree.addStation(st1);
		}
		
		for(Router router:stRoot.getListRouter())
		{
			firstNodeChild = nodeChild;
			builbTreeNode(stTree,nodeChild, router);			
			nodeChild = firstNodeChild;
		}		
		return stTree;		
	}

	private void builbTreeNode(StationContainer container, Node nodeChild, Router router)
	{
		// get the station of the same network
		for(Station st:router.getListStation())
		{
			if(nodeChild.getNum().equals("null"))
			{
				// first station of network begin the head
				nodeChild = container.addStation(st);
			}
			else
			{	
				// and other begin children
				nodeChild.addStation(st);				
			}
		}
	
		for(Router routerChild:router.getListRooter())
		{
			StationContainer ct=nodeChild;
			
			// if the router don't contain the station
			if(nodeChild.getNum().equals("null"))
			{
				ct=container;
			}
			// recursivity
			builbTreeNode(ct,new Node(), routerChild);
		}
	}

}
