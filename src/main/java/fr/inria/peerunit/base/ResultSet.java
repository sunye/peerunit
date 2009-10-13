/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import fr.inria.peerunit.parser.MethodDescription;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sunye
 */
public class ResultSet implements Serializable {

    private Map<MethodDescription,List<Result>> results;

    public ResultSet () {
        results = new Hashtable<MethodDescription, List<Result>>();
    }
    
    public void add(Result r) {
        MethodDescription md = r.getMethodDescription();
        
        if (!results.containsKey(md)) {
            results.put(md, new LinkedList<Result>());
        }
        results.get(md).add(r);
        
    }
}
