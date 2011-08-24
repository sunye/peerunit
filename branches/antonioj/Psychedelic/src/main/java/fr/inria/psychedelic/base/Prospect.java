package fr.inria.psychedelic.base;

import java.io.Serializable;

/**
 * @author sunye
 */
public class Prospect implements Serializable {

    /**
     * Class name.  The name of a class is its fully qualified name
     * (as returned by Class.getName(), where '.' are replaced by '/'.
     */
    private final String className;
    private final String methodName;
    private final String description;
    private final int lineNumber;
    private final int operation;
    private final int order;

    public Prospect(String cn, String mn, String desc, int i, int o, int odr) {
        className = cn;
        methodName = mn;
        description = desc;
        lineNumber = i;
        operation = o;
        order = odr;
    }

    @Override
    public String toString() {
        return className + "::" + methodName + " description: " + description + " line: " + lineNumber + " operation: "
                + operation + " order: " + order;
    }

    public boolean matchesMethod(String name, String desc) {
        return methodName.equals(name) && description.equals(desc);
    }

    public boolean matchesInstruction(int line, int op, int od) {
        return (lineNumber == line) && (operation == op) && (order == od);
    }
}
