package fr.inria.peerunit.onstree.stationTree;

import java.util.List;


/**
 * @author Booba
 * interface Node
 */
public interface StationContainer
{
	Node addStation(Station st);
//	StationRoot getStRoot();
	int getChildrenNumber();
	List<Node> getListChildStation();
	AbstractStation getStation();  // XXX	
}
