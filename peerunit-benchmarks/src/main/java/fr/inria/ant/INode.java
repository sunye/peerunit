package fr.inria.ant;

import org.apache.tools.ant.Task;

public interface INode extends IAbstractNode, Comparable<INode> {
    public Task createDeploy(DeployTask deploy);

    public Task execute(ExecuteTask remote);

    public void verify();

    public String getIp();

    public void setIp(String ip);
}
