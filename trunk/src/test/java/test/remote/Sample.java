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
package test.remote;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.TestStep;
import static fr.inria.peerunit.test.assertion.Assert.*;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test TesterImpl and CoordinatorImpl
 */
public class Sample extends TestCaseImpl {

	@TestStep(place = -1, timeout = 1000, name = "action1", step = 1)
	public void first() {
		System.setProperty("executed", "ok");
	}

	
	@TestStep(place = -1, timeout = 1000, name = "action2", step = 2)
	public void error() {
		assertTrue(false);
	}
	
	@TestStep(place = -1, timeout = 1000, name = "action3", step = 3)
	public void assertionError() {
		assert false;
	}	

        @Override
	public void setTester(Tester ti) {

		// TODO Auto-generated method stub
		
	}

}
