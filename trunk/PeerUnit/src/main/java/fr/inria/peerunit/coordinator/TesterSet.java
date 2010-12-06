/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.common.MethodDescription;

/**
 *
 * @author sunye
 */
public interface TesterSet {

    void execute(String str) throws InterruptedException;

    void execute(MethodDescription md) throws InterruptedException;

    Schedule getSchedule();
}
