
package fr.inria.ant;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;

public class NodeSetData extends AbstractNodeData implements INodeSet {
    
    private Set<IAbstractNode> nodes = new HashSet<IAbstractNode>();
    
    /**
     * task attributes
     */

    private String ip_from;
    private String ip_to;
    private String includes;
    private String excludes;
    private Set<String> ex;
    private Set<Include> in = new HashSet<Include>();

    // Task attributes
    
    public String getFrom() {
            return ip_from;
    }

    public void setFrom(String ip_from) {
        this.ip_from = ip_from;
    }

    public String getTo() {
            return ip_to;
    }

    public void setTo(String ip_to) {
        this.ip_to = ip_to;
    }
    

    private void expand() {

        
        
        
        /**
         * Traitement de l'attributs includes et excludes
         */
        resolveIncludes();

        resolveExcludes();

        /**
         * Cr�ation des noeuds distants
         */
        if (ip_from != null && ip_to != null && this.getDirectory() != null) {
            //createHostSet();
        }
    }

    /** Permet de cr�er les machines distantes * */
    private void createHostSet() {
        assert ip_from != null : "NodeSetImpl::createHostSet() Null ip_from";
        
        /** V�rification de la formation des adresses ip * */
        StringTokenizer stFrom = new StringTokenizer(ip_from, ".");
        if (stFrom.countTokens() != 4) {
            throw new BuildException("l'ip from est mal form�");
        }

        String ipFrom1 = stFrom.nextToken();
        String ipFrom2 = stFrom.nextToken();
        String ipFrom3 = stFrom.nextToken();
        String ipFrom4 = stFrom.nextToken();

        StringTokenizer stTo = new StringTokenizer(ip_to, ".");
        if (stTo.countTokens() != 4) {
            throw new BuildException("L'ip to est mal form�");
        }
        String ipTo1 = stTo.nextToken();
        String ipTo2 = stTo.nextToken();
        String ipTo3 = stTo.nextToken();
        String ipTo4 = stTo.nextToken();

        if (!ipFrom1.equals(ipTo1) || !ipFrom2.equals(ipTo2)
                || !ipFrom3.equals(ipTo3)) {
            throw new BuildException("from et to non compatible");
        }

        if (Integer.parseInt(ipFrom4) > Integer.parseInt(ipTo4)) {
            throw new BuildException(
                    "La derni�re partie de l'ip de l'attribut from est sup�rieure � celle de l'attribut to");
        }

        /** construction des machines distantes * */
        for (int i = Integer.parseInt(ipFrom4); i <= Integer.parseInt(ipTo4); i++) {
            String ipTemp = ipFrom1 + "." + ipFrom2 + "." + ipFrom3 + "." + i;
            if (!getEx().contains(ipTemp)) {
                Node md = new Node(this);
                md.setIp(ipTemp);
                if (!nodes.contains(md))
                    nodes.add(md);
            }
        }
 
     }

    /** Permet d'ajouter les machines distantes definies dans l'attribut includes * */
    private void resolveIncludes() {
        
        // L'utilisateur a pr�cis� d'autres noeuds
        if (getIncludes() != null) {
            String[] ips = getIncludes().split(";");
            int i = 0;
            while (i < ips.length) {
                Node md = new Node(this);
                md.setIp(ips[i]);
                if (!nodes.contains(md)) {
                    nodes.add(md);
                }
                i++;
            }
        }
    }

    /**
     * Permet de retirer les machines distantes d�finies dans l'attribut
     * excludes *
     */
    private void resolveExcludes() {
        
        
        setEx(new HashSet<String>());
        if (getExcludes() != null) {
            String[] ips = getExcludes().split(";");
            int i = 0;
            while (i < ips.length) {
                getEx().add(ips[i]);
                i++;
            }
        }
    }

    public Set<IAbstractNode> getNodes() {
            return nodes;
    }

    public String getIncludes() {
            return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
            return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /** Permet d'initialiser la tache enfant include * */
    public Include createInclude() {
        
        Include i = new Include();
        getIn().add(i);
        return i;
    }
    
    @Override
    public void verify() {
        super.verify();
        if (getFrom() != null && getFrom().split("\\.").length != 4) {
            throw new BuildException("Invalid from attribute");
        }
 
        if (getTo() != null && getTo().split("\\.").length != 4) {
            throw new BuildException("Invalid to attribute");
        }
        
        
        this.expand();
        for (IAbstractNode h : this.getNodes()) {
            h.verify();
        }
    }


    private void setIn(Set<Include> in) {
        this.in = in;
    }

    private Set<Include> getIn() {
            return in;
    }

    private void setEx(Set<String> ex) {
        this.ex = ex;
    }

    private Set<String> getEx() {
            return ex;
    }
    
    protected INodeSet getReference() {
        throw new RuntimeException("Should not Implement");
    }

 
    public Task createDeploy(DeployTask deploy) {
        throw new RuntimeException("Should not Implement");
    }

  
    public Task execute(ExecuteTask remote) {
        throw new RuntimeException("Should not Implement");
    }

    public void addNode(Node n) {
        nodes.add(n);
        
    }

    public Task executeJava(CommandlineJava command) {
        throw new RuntimeException("Should not Implement");
    }

}

