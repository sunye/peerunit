/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
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

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test the class ParserImpl
 */
public class Data extends TestCaseImpl {

    @BeforeClass(place = -1, timeout = 100)
    public void begin() {
    }

    @TestStep(place = -1, timeout = 1000, name = "action1", step = 1)
    public void startingNetwork() {
    }

    @TestStep(place = -1, timeout = 1000000, name = "action1", step = 2)
    public void joinNet() {
    }

    @TestStep(place = -1, timeout = 1000000, name = "action2", step = 0)
    public void stabilize() {
    }

    @TestStep(place = -1, timeout = 1000000, name = "action3", step = 0)
    public void put() {
    }

    @TestStep(place = -1, timeout = 1000000, name = "action4", step = 0)
    public void get() {
    }

    @TestStep(place = 42, timeout = 1000000, name = "action4", step = 0)
    public void notHere() {
    }

    @TestStep(from = 10, to = 100, timeout = 1000000, name = "action4", step = 0)
    public void alsoNotHere() {
    }

    @TestStep(from = 0, to = 100, timeout = 1000000, name = "action4", step = 0)
    public void here() {
    }

    @AfterClass(timeout = 100, place = -1)
    public void end() {
    }

    @Override
    public void setTester(Tester ti) {
        // TODO Auto-generated method stub
    }
}
