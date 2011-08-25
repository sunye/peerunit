/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit;

/**
 * @author sunye
 */
public class Globals {

    private static int id = -1;

    /**
     * Sets the Id of the current tester. Should be called only once.
     *
     * @param i a positive integer.
     */
    public static void setId(int i) {
        //assert id == -1 : "Trying to change the Id";

        id = i;
    }
}