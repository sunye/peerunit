/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.psychedelic.gathering;

import fr.inria.psychedelic.base.Mutations;
import org.objectweb.asm.tree.ClassNode;
import fr.inria.psychedelic.base.Prospect;
import java.util.Iterator;
import java.util.ListIterator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author sunye
 */
public class ClassMutator {

    public void apply(ClassNode node, Prospect prospect) {
        MethodNode method = null;
        AbstractInsnNode insn = null;
        int linenumber = -1;
        int order = 0;

        for (Iterator it = node.methods.iterator(); it.hasNext();) {
            method = (MethodNode) it.next();
            if (prospect.matchesMethod(method.name, method.desc)) {
                break;
            }
        }


        ListIterator it = method.instructions.iterator();
        boolean leave = false;
        while (it.hasNext() && !leave) {
            insn = (AbstractInsnNode) it.next();

            switch(insn.getType()) {
                case AbstractInsnNode.LINE:
                    LineNumberNode line = (LineNumberNode) insn;
                    linenumber = line.line;
                    break;
                case AbstractInsnNode.INSN:
                    order++;
                    leave = prospect.matchesInstruction(linenumber,
                            insn.getOpcode(), order);
                    break;
            }
        }

        method.instructions.set(insn, new InsnNode(Mutations.mutate(insn.getOpcode())));

    }
}
