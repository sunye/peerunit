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
package fr.inria.peerunit.parser;

import java.io.Serializable;

/**
 * @author sunye
 * 
 */
public class MethodDescription implements Comparable<MethodDescription>,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Method name
     */
    private String name;
    private int order;
    /**
     * Method execution timeout (in milliseconds).
     */
    private int timeout;

    /*
     * Create a method description
     */
    public MethodDescription(String name, int order, int timeout) {
        this.timeout = timeout;
        this.name = name;
        this.order = order;
    }

    public MethodDescription(TestStepMethod method) {
        this(method.method().getName(), method.order(), method.timeout());
    }

    public MethodDescription(BeforeClassMethod method) {
        this(method.method().getName(), Integer.MIN_VALUE, method.timeout());
    }

    public MethodDescription(AfterClassMethod method) {
        this(method.method().getName(), Integer.MAX_VALUE, method.timeout());
    }


    /*
     * Compares the order of this object with the order of the specified object for order.
     *
     * @param o - the MethodDescription to be compared.
     * @return int
     *  -1 if the order of this object is less than the order of the specified object
     *  0 if the order of this object is equal to the order of the specified object
     *  1 if the order of this object is greater to the order of the specified object
     */
    public int compareTo(MethodDescription other) {

        if (order < other.order) {
            return -1;
        }

        if (order > other.order) {
            return 1;
        }

        return name.compareTo(other.name);
    }

    /*
     * Returns a string representation of the method description . The toString
     * method returns a string consisting of the name, the test case and the
     * order of the class of which the object is an instance
     *
     * @return String a string representation of the method description
     */
    @Override
    public String toString() {
        return String.format("Method: %s Order: %d", name, order);
    }

    /*
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o - the reference object with which to compare.
     * @return boolean - true if this object is the same as the object argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodDescription)) {
            return false;
        } else {
            MethodDescription other = (MethodDescription) o;
            return name.equals(other.name) &&
                    order == other.order &&
                    timeout == other.timeout;
        }
    }

    /*
     * Returns the name associated to method
     * @return String
     */
    public String getName() {
        return name;
    }

    /*
     * Returns the method execution timeout (in milliseconds)
     * @return int
     */
    public int getTimeout() {
        return timeout;
    }

    public int getOrder() {
        return order;
    }
}
