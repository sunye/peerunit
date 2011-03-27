/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.psychedelic.gathering;

import fr.inria.psychedelic.base.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author sunye
 */
public class ProspectGatheringClassAdapter extends ClassAdapter {

    private List<Prospect> prospects =
            new LinkedList<Prospect>();

    private String classname;

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
        classname = name;

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

    class ProspectGatheringMethodAdapter extends MethodAdapter {

        /**
         * The name of the method that is beeing visited.
         */
        private String methodname;

        private String description;

        /**
         * The number of the last visited line;
         */
        private int linenumber = -1;

        private int order = 0;

        public ProspectGatheringMethodAdapter(MethodVisitor mv, String name, String desc) {
            super(mv);
            methodname = name;
            description = desc;
        }

        @Override
        public void visitInsn(int opcode) {
            order++;
            if (Mutations.canMutate(opcode)) {
                prospects.add(new Prospect(classname, methodname, description, linenumber, opcode, order));
            }

            mv.visitInsn(opcode);
        }

        @Override
        public void visitLineNumber(int i, Label label) {
            linenumber = i;
            mv.visitLineNumber(i, label);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            mv.visitJumpInsn(opcode, label);
        }
    }
}
