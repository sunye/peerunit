package fr.inria.peerunit.onstree.testerTree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.IsInstanceOf;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

/*
 * @author Jeremy
 * @author Aboubakar Koïta  
 */

public class TesterNodeHead extends AbstractBTreeNode implements TesterContainer, Serializable
{
	private static final long	serialVersionUID	= 1L;
	private Integer id;
	private String sColor;
	private TesterNodeHead parent;
	private TesterNode childL;
	private TesterNode childR;
	private int equilibre;
	private List<TesterNodeHead> listTesterNodeHead;
	private String ip=null;
	
	public TesterNodeHead()
	{
		listTesterNodeHead = new ArrayList<TesterNodeHead>();
		parent = null;
	}
	
	public TesterNodeHead(int id, String color, TesterNode childL,
			TesterNode childR, int equilibre)
	{
		this.id = id;
		sColor = color;
		this.childL = childL;
		this.childR = childR;
		this.equilibre = equilibre;
		listTesterNodeHead = new ArrayList<TesterNodeHead>();
	}
	
	public TesterNodeHead(String ip, int id, String color, TesterNode childL,
			TesterNode childR, int equilibre)
	{
		this.ip=ip;
		this.id = id;
		sColor = color;
		this.childL = childL;
		this.childR = childR;
		this.equilibre = equilibre;
		listTesterNodeHead = new ArrayList<TesterNodeHead>();
	}
	

	public void addNodeHead(TesterNodeHead testNH)
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
		
		for(TesterNodeHead testerNH:listTesterNodeHead)
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
		for(TesterNodeHead nodeHead:listTesterNodeHead)
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
//		int childrenNumber = getNodesSize();
		int counter = 0;
		List<Comparable> tab_Keys = new ArrayList<Comparable>();
		if(childL != null)
		{
			for(Comparable cp:childL.getKeys())
			{
				tab_Keys.add(cp);
			}
		}
		tab_Keys.add((Comparable) id);
		if(childR != null)
		{
			for(Comparable cp:childR.getKeys())
			{
				tab_Keys.add(cp);
			}
		}
/*
		for(TesterNodeHead_be nodeHead:listTesterNodeHead)
		{
			for(Comparable cp:nodeHead.getKeys())
			{
				tab_Keys[counter++] = cp;
			}
		}*/
		Comparable[] comparables=new Comparable[tab_Keys.size()];
		int cpt=0;
		for (Comparable comparable:tab_Keys)
		{
			comparables[cpt++]=comparable;
		}
		
		return  comparables;
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
		if((childL == null) && (childR == null)&& !isRoot() )
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
		if(parent == null)
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
		if(i.intValue()==id.intValue())
		{
			aBTNode = this;
		}
		else if((i.intValue() < id.intValue()) && (childL != null))
		{
			aBTNode = childL.getNode(i);
		} else 	if((i.intValue() > id.intValue()) && (childR != null))
		{
			aBTNode = childR.getNode(i);
		} else 	if(aBTNode == null)
		{
			for(TesterNodeHead nodeHead:listTesterNodeHead)
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
		int childrenNumber = 1;
		
		if(childL != null)
		{
			childrenNumber += childL.getNodesSize();
		}
		
		if(childR != null)
		{
			childrenNumber += childR.getNodesSize();
		}
		
		for(TesterNodeHead nodeHead:listTesterNodeHead)
		{
			childrenNumber += nodeHead.getNodesSize();
		}
		
		return childrenNumber;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.onstree.testerTree.TesterContainer#add(long)
	 */
	public void add(long id)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.onstree.testerTree.TesterContainer#getChildL()
	 */
	public TesterNode getChildL()
	{
		return childL;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.onstree.testerTree.TesterContainer#getChildR()
	 */
	public TesterNode getChildR()
	{
		return childR;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.onstree.testerTree.TesterContainer#setParent(fr.inria.peerunit.btreeStrategy.AbstractBTreeNode)
	 */
	public void updateParent(AbstractBTreeNode parent)
	{
		// update parent himself
		if(parent != null && parent instanceof TesterNodeHead)
		{
			this.parent = (TesterNodeHead) parent;
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
		
		for(TesterNodeHead nodeHead:listTesterNodeHead)
		{
			nodeHead.updateParent(this);
		}
	}

	public String getIP() {
		return ip;
	}

	public List<TesterNodeHead> getListTesterNodeHead() {
		return listTesterNodeHead;
	}

	public void setListTesterNodeHead(List<TesterNodeHead> listTesterNodeHead) {
		this.listTesterNodeHead = listTesterNodeHead;
	}
}
