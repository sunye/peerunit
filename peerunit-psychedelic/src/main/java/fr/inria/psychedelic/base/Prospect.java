
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package fr.inria.psychedelic.base;

//~--- non-JDK imports --------------------------------------------------------

import org.objectweb.asm.tree.MethodNode;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 *
 * @author sunye
 */
public class Prospect implements Serializable {

    /**
     * Class name.  The name of a class is its fully qualified name
     * (as returned by Class.getName(), where '.' are replaced by '/'.
     */
    final private String classname;
    final private String description;
    final private int    linenumber;
    final private String methodname;
    final public int     operation;
    final private int    order;

    public Prospect(String cn, String mn, String desc, int i, int o, int odr) {
        classname   = cn;
        methodname  = mn;
        description = desc;
        linenumber  = i;
        operation   = o;
        order       = odr;
    }

    @Override
    public String toString() {
        return classname + "::" + methodname + " description: " + description + " line: " + linenumber + " operation: "
               + operation + " order: " + order;
    }

    public boolean matchesMethod(String name, String desc) {
        return methodname.equals(name) && description.equals(desc);
    }

    public boolean matchesInstruction(int line, int op, int od) {
        return (linenumber == line) && (operation == op) && (order == od);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
