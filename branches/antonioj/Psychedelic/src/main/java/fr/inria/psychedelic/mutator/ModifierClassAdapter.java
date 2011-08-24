/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.psychedelic.mutator;

import org.objectweb.asm.Label;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author sunye
 */
public class ModifierClassAdapter extends ClassAdapter {

    public ModifierClassAdapter(ClassVisitor cv) {
        super(cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new ModifierMethodAdapter(mv);
        }
        return mv;
    }

    class ModifierMethodAdapter extends MethodAdapter {

    private int linenumber = -1;


    public ModifierMethodAdapter(MethodVisitor mv) {
        super(mv);
    }

    @Override
    public void visitInsn(int opcode) {

        if (opcode == Opcodes.ISUB) {
            System.out.println("ISUB");
            mv.visitInsn(Opcodes.IADD);
        } else if (opcode == Opcodes.IADD) {
            System.out.println("IADD");
            mv.visitInsn(Opcodes.ISUB);
        } else {
            mv.visitInsn(opcode);
        }
    }

    @Override
    public void visitLineNumber(int i, Label label) {
        linenumber = i;
        mv.visitLineNumber(i, label);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        System.out.println("opcode:" + opcode + "owner" + owner + "name"+ name + "desc" +desc);
        mv.visitMethodInsn(opcode, owner, name, desc);
    }


}

}
