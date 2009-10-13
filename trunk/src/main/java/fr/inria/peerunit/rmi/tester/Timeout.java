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
package fr.inria.peerunit.rmi.tester;

/**
 * @author Eduardo Almeida.
 * @author sunye
 * @version 1.0
 * @since 1.0
 * @see java.lang.Runnable
 * @see java.lang.Thread
 */
public class Timeout implements Runnable {

    private long timeout;
    private Thread thread;

    public Timeout(Thread t, long millis) {
        timeout = millis;
        thread = t;
    }

    /**
     * measure the life time of an thread
     *
     * @param t the tester which will be registered
     * @param list the MethodDescription list
     * @see fr.inria.peerunit.Coordinator#register(fr.inria.peerunit.Tester,fr.inria.peerunit.parser.MethodDescription)
     * @throws InterruptedException
     */
    public void run() {
        try {
            thread.join(timeout);
            if (thread.isAlive()) {
                thread.interrupt();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
