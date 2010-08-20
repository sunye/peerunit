/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class RangeTest {
    

    @Test
    public void testStar() {
        Range star = Range.fromString("*");
        assertTrue(star.includes(0));
        assertTrue(star.includes(Integer.MAX_VALUE));
        assertTrue(star.includes(42));
        assertFalse(star.includes(-1));
        assertFalse(star.includes(Integer.MIN_VALUE));
    }

    @Test
    public void testSingle() {
        Range r = Range.fromString("33");
        int[] wrong = {0,-1,32,34,Integer.MAX_VALUE};

        assertTrue(r.includes(33));

        for(int each : wrong) {
            assertFalse(r.includes(each));
        }
    }

    @Test
    public void testInterval() {
        Range r = Range.fromString("13-32");
        int[] wrong = {0,-1,12,33,Integer.MAX_VALUE,Integer.MIN_VALUE};
        int[] right = {13,32,16,20,25,29};

        for(int each : wrong) {
            assertFalse(r.includes(each));
        }

        for(int each : right) {
            assertTrue("Must include: "+each, r.includes(each));
        }

    }
}
