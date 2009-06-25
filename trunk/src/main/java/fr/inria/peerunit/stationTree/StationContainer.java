package fr.inria.peerunit.stationTree;

import java.util.List;


/**
 * @author Bouba
 * interface Node
 */
public interface StationContainer
{
	public Node addStation(Station st);
	public StationRoot getStRoot();
	public int getChildrenNumber();
	public List<Node> getListChildStation();
}
