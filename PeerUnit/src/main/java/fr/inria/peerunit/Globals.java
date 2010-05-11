/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit;

/**
 *
 * @author sunye
 */
public class Globals {

    private static int id = -1;

    public static int getId() {
        assert id >= 0 : "Trying to get an Id befor having one";

        
        return -1;
    }

    public static void setId(int i) {
        assert id == -1 : "Trying to change the Id";

        id = i;
    }
}
