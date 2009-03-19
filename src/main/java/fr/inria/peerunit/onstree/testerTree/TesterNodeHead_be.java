package fr.inria.peerunit.onstree.testerTree;

import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.btree.AbstractBTreeNode;



public class TesterNodeHead_be extends AbstractBTreeNode
{
	private int id;
	private String sColor;
	private TesterNodeHead_be parent;
	private TesterNode_be childL;
	private TesterNode_be childR;
	private int equilibre;
	private List<TesterNodeHead_be> listTesterNodeHead;
	
	public TesterNodeHead_be()
	{
		listTesterNodeHead = new ArrayList<TesterNodeHead_be>();
	}
	
	public TesterNodeHead_be(int id, String color, TesterNode_be childL,
			TesterNode_be childR, int equilibre)
	{
		this.id = id;
		sColor = color;
		this.childL = childL;
		this.childR = childR;
		this.equilibre = equilibre;
		listTesterNodeHead = new ArrayList<TesterNodeHead_be>();
	}

	public void addNodeHead(TesterNodeHead_be testNH)
	{
		listTesterNodeHead.add(testNH);
	}
	
	public int getId()
	{
		return id;
	}

	public StringBuffer graphicPrintTester()
	{
		StringBuffer tree=new StringBuffer("\t"+"node [shape=box, style=filled, color="+sColor+"]; "+getId()+";");
		
		if(childL!=null)
		{
			tree.append(childL.graphicPrintTester(sColor));
			tree.append("\t"+getId()+" -> "+
					childL.getId()+";");	
		}
		
		if(childR!=null)
		{
			tree.append(childR.graphicPrintTester(sColor));
			tree.append("\t"+getId()+" -> "+
					childR.getId()+";");	
		}
		
		for(TesterNodeHead_be testerNH:listTesterNodeHead)
		{
			tree.append(testerNH.graphicPrintTester());
			tree.append("\t"+getId()+" -> "+
					testerNH.getId()+";");	
		}
		
		return tree;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.AbstractBTreeNode#getChildren()
	 */
	@Override
	public AbstractBTreeNode[] getChildren()
	{
		int nbNode = listTesterNodeHead.size()+2;
		int counter = 2;
		AbstractBTreeNode[] tab_aBTNode = new AbstractBTreeNode[nbNode];
		tab_aBTNode[0] = childL;
		tab_aBTNode[1] = childR;
		for(TesterNodeHead_be nodeHead:listTesterNodeHead)
		{
			tab_aBTNode[counter++] = nodeHead;
		}
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

		for(TesterNodeHead_be nodeHead:listTesterNodeHead)
		{
			for(Comparable cp:nodeHead.getKeys())
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
		return parent;
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
		if(parent != null)
		{
			return true;
		}
		
		return false;
	}

	/** Return the node (Tester) following the id number
	 * @param i id number of the Tester
	 * @return
	 */
	public AbstractBTreeNode getNode(Integer i)
	{
		AbstractBTreeNode aBTNode = null;
		if(i == id)
		{
			aBTNode = this;
		}
		if((i < id) && (childL != null))
		{
			aBTNode = childL.getNode(i);
		}
		if((i > id) && (childR != null))
		{
			aBTNode = childR.getNode(i);
		}
		if(aBTNode == null)
		{
			for(TesterNodeHead_be nodeHead:listTesterNodeHead)
			{
				aBTNode = nodeHead.getNode(i);
				if(aBTNode != null)
				{
					return aBTNode;
				}
					
			}
		}
		
		return aBTNode;
	}

	/** Return the children number
	 * @return
	 */
	public int getNodesSize()
	{
		int childrenNumber = 0;
		
		childrenNumber = childL.getNodesSize() + childR.getNodesSize();
		for(TesterNodeHead_be nodeHead:listTesterNodeHead)
		{
			childrenNumber += nodeHead.getNodesSize();
		}
		
		return childrenNumber;
	}
}
