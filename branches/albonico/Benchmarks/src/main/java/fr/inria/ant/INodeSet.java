/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

import java.util.Set;

public interface INodeSet extends IAbstractNode {
    public String getFrom();

    public boolean isChecked();

    public void setFrom(String ip_from);

    public String getTo();

    public void setTo(String ip_to);

    public Set<IAbstractNode> getNodes();

    //public Include createInclude();

    public void verify();

    // public void addNode(Node n);

}
