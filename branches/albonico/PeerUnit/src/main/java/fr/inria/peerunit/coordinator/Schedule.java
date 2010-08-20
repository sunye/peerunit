/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author sunye
 */
public class Schedule {

    private Map<MethodDescription, Set<Tester>> testerMap;

    public Schedule() {
        testerMap = Collections.synchronizedMap(new TreeMap<MethodDescription, Set<Tester>>());
    }

    public void put(MethodDescription md, Tester t) {
        if (!testerMap.containsKey(md)) {
            testerMap.put(md, new HashSet<Tester>());
        }
        testerMap.get(md).add(t);
    }

    public void put(TesterRegistration tr) {
        for(MethodDescription each : tr.methods()) {
            this.put(each,tr.tester());
        }
    }

    public void clear() {
        testerMap.clear();
    }

    public boolean containsMethod(MethodDescription md) {
        return testerMap.containsKey(md);
    }
    public int size() {
        return testerMap.size();
    }

    public Collection<MethodDescription> methods() {
        return new ArrayList<MethodDescription>(testerMap.keySet());
    }

    public Collection<Tester> testersFor(MethodDescription md) {
        return testerMap.get(md);
    }
}
