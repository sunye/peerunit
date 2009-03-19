package fr.inria.peerunit.onstree.testerTree;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

public class TesterNode_be extends AbstractBTreeNode implements TesterContainer
{
	
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

	public void add(long id)
	{

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
	@Override
	public AbstractBTreeNode[] getChildren()
	{
		AbstractBTreeNode[] tab_aBTNode = new AbstractBTreeNode[2];
		tab_aBTNode[0] = childL;
		tab_aBTNode[1] = childR;
		return tab_aBTNode;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#getKeys()
	 */
	@Override
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
	@Override
	public AbstractBTreeNode getParent()
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
	@Override
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
	@Override
	public boolean isRoot()
	{
		return false;
	}

	/** Return the node (Tester) following the id number
	 * @param i id number of the Tester
	 */
	public AbstractBTreeNode getNode(Integer i)
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
}
