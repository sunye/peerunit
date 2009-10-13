/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
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
