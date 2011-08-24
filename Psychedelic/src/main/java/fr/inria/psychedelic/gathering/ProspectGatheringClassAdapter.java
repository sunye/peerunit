/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.psychedelic.gathering;

import fr.inria.psychedelic.base.Mutations;
import fr.inria.psychedelic.base.Prospect;
import org.objectweb.asm.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sunye
 */
public class ProspectGatheringClassAdapter extends ClassAdapter {

    private final List<Prospect> prospects = new LinkedList<Prospect>();
    private String className;

    public ProspectGatheringClassAdapter(ClassVisitor cv) {
        super(cv);
    }

    public List<Prospect> getProspects() {
        return Collections.unmodifiableList(prospects);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {

        cv.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new ProspectGatheringMethodAdapter(mv, name, desc);
        }
        return mv;
    }

    private class ProspectGatheringMethodAdapter extends MethodAdapter {

        /**
         * The name of the method that is being visited.
         */
        private final String methodName;
        private final String description;

        /**
         * The number of the last visited line;
         */
        private int lineNumber = -1;
        private int order = 0;

        public ProspectGatheringMethodAdapter(MethodVisitor mv, String name, String desc) {
            super(mv);
            methodName = name;
            description = desc;
        }

        @Override
        public void visitInsn(int opcode) {
            order++;
            /**
             * Method run on main Job class should not be mutated.
             * At this moment this check will prevent any method
             * named run of being mutated.
             */
            if (Mutations.canMutate(opcode) && !methodName.equals("run")) {
                prospects.add(new Prospect(className, methodName, description, lineNumber, opcode, order));
            }
            mv.visitInsn(opcode);
        }

        @Override
        public void visitLineNumber(int i, Label label) {
            lineNumber = i;
            mv.visitLineNumber(i, label);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            mv.visitJumpInsn(opcode, label);
        }
    }
}
