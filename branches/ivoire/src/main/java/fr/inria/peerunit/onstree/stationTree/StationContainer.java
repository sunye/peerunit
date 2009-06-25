package fr.inria.peerunit.onstree.stationTree;

import java.util.List;


/**
 * @author Bouba
 * interface Node
 */
public interface StationContainer
{
	Node addStation(Station st);
	StationRoot getStRoot();
	int getChildrenNumber();
	List<Node> getListChildStation();
	Station getStation();  // XXX	
}
