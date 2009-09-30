package fr.inria.peerunit.onstree.testerTree;

import fr.inria.peerunit.util.BTreeNode;

/** Tester node of type container (TesterNodeHead, TesterNode)
 * @author Jeremy Masson
 *
 */
public interface TesterContainer
{
	
	/** Return the child Right of the tree
	 * @return an object TesterNode_be
	 */
	public TesterNode_be getChildR();
	
	/** Return the child Left of the tree
	 * @return an object TesterNode_be
	 */
	public TesterNode_be getChildL();
	
	
	/** Set the parent and update parents of children.
	 * To use only after that tree is built
	 * @param parent AbstractBTreeNode
	 */
	public void updateParent(BTreeNode parent);
}
