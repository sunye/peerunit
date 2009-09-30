package fr.inria.peerunit.onstree.testerTree;

import java.io.Serializable;

import fr.inria.peerunit.util.BTreeNode;

/** This tree node follows to a tester. It can be a child of the head node of a station
 * tree type: balance binary tree
 * @author Jeremy Masson
 *
 */
public class TesterNode_be  implements BTreeNode,TesterContainer, Serializable
{
	private static final long	serialVersionUID	= 1L;
	public int id;
	private TesterNodeHead_be parentNodeHead;
	private TesterNode_be parent;
	public TesterNode_be childL;
	public TesterNode_be childR;
	public int equilibre;
	
	public TesterNode_be()
	{
		childL = null;
		childR = null;
		equilibre = 0;
	}
	
	public TesterNode_be(int lId, TesterNode_be g, TesterNode_be d)
	{
		id = lId;
		childL = g;
		childR = d;
		equilibre = 0;
	}

	public StringBuffer graphicPrintTester(String color)
	{
		StringBuffer tree=new StringBuffer("\t"+"node [shape=circle, style=filled, color="+color+"]; "+id+";");
		
		if(childL!=null)
		{
			tree.append(childL.graphicPrintTester(color));
			tree.append("\t"+id+" -> "+
					childL.id+";");	
		}
		
		if(childR!=null)
		{
			tree.append(childR.graphicPrintTester(color));
			tree.append("\t"+id+" -> "+
					childR.id+";");	
		}
		
		return tree;
	}
	
	public int getId()
	{
		return id;
	}

	public TesterNode_be getChildL()
	{
		return childL;
	}

	public TesterNode_be getChildR()
	{		
		return childR;
	}
	
	public int getEquilibre()
	{
		return equilibre;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#getChildren()
	 */
	public BTreeNode[] getChildren()
	{
		BTreeNode[] tab_aBTNode = new BTreeNode[2];
		tab_aBTNode[0] = childL;
		tab_aBTNode[1] = childR;
		return tab_aBTNode;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#getKeys()
	 */
	public Comparable[] getKeys()
	{
		int childrenNumber = getNodesSize();
		int counter = 0;
		Comparable[] tab_Keys = new Comparable[childrenNumber];
		if(childL != null)
		{
			for(Comparable cp:childL.getKeys())
			{
				tab_Keys[counter++] = cp;
			}
		}
		tab_Keys[counter++] = (Comparable) id;
		if(childR != null)
		{
			for(Comparable cp:childR.getKeys())
			{
				tab_Keys[counter++] = cp;
			}
		}
		
		return tab_Keys;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#getParent()
	 */
	
	public BTreeNode getParent()
	{
		if(parentNodeHead != null)
		{
			return parentNodeHead;
		}
		if(parent != null)
		{
			return parent;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#isLeaf()
	 */
	
	public boolean isLeaf()
	{
		if((childL == null) && (childR == null))
		{
			return true;
		}
				
		return false;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#isRoot()
	 */
	
	public boolean isRoot()
	{
		return false;
	}

	/** Return the node (Tester) following the id number
	 * @param i id number of the Tester
	 */
	public BTreeNode getNode(Integer i)
	{
		if(i == id)
		{
			return this;
		}
		if((i < id) && (childL != null))
		{
			return childL.getNode(i);
		}
		if((i > id) && (childR != null))
		{
			return childR.getNode(i);
		}
		
		return null;
	}

	/**
	 * @return
	 */
	public int getNodesSize()
	{
		int childrenNumber = 1;
		
		if(childL != null)
		{
			childrenNumber += childL.getNodesSize();
		}
		if(childR != null)
		{
			childrenNumber += childR.getNodesSize();
		}
		
		return childrenNumber;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.onstree.testerTree.TesterContainer#setParent(fr.inria.peerunit.btreeStrategy.AbstractBTreeNode)
	 */
	public void updateParent(BTreeNode parent)
	{
		// update parent himself
		if(parent instanceof TesterNodeHead_be)
		{
			this.parentNodeHead = (TesterNodeHead_be) parent;
		}
		if(parent instanceof TesterNode_be)
		{
			this.parent = (TesterNode_be) parent;
		}	
		// update parent of his children
		if(childL != null)
		{
			childL.updateParent(this);
		}
		if(childR != null)
		{
			childR.updateParent(this);
		}
	}
}
