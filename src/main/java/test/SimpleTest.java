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
package test;


import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;

public class SimpleTest extends TestCaseImpl{	
	
	/**
	 * This method starts the test
	 */
	@BeforeClass(place=-1,timeout=100)
	public void begin(){
			
	}


	@TestStep(place=-1,timeout=1000, name = "action2", step = 1)
	public void action2(){
		
	}


	@TestStep(from=1,to=3,timeout=1000000, name = "action3", step = 1)
	public void action3(){
	
	}

	@TestStep(place=2,timeout=1000000, name = "action4", step = 1)
	public void action4(){
	
	}
	@TestStep(place=-1,timeout=1000000, name = "action5", step = 1)
	public void action5(){
	
	}
	@TestStep(place=-1,timeout=1000000, name = "action6", step = 1)
	public void action6(){
	
	}
	@TestStep(place=-1,timeout=1000000, name = "action7", step = 1)
	public void action7(){
	
	}
	
	@AfterClass(timeout=100,place=-1)
	public void end() {
	
	}


	public void setTester(Tester t) {
		// TODO Auto-generated method stub
		
	}

}

