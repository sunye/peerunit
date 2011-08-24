/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.psychedelic.gathering;

import fr.inria.psychedelic.base.Mutations;
import fr.inria.psychedelic.base.Prospect;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * @author sunye
 */
public class ClassMutator {

    public void apply(ClassNode node, Prospect prospect) {
        MethodNode method = null;
        AbstractInsnNode insn = null;
        int lineNumber = -1;
        int order = 0;

        for (Object method1 : node.methods) {
            method = (MethodNode) method1;
            if (prospect.matchesMethod(method.name, method.desc)) {
                break;
            }
        }

        ListIterator it = method.instructions.iterator();
        boolean leave = false;

        while (it.hasNext() && !leave) {
            insn = (AbstractInsnNode) it.next();
            switch (insn.getType()) {
                case AbstractInsnNode.LINE:
                    LineNumberNode line = (LineNumberNode) insn;
                    lineNumber = line.line;
                    break;
                case AbstractInsnNode.INSN:
                    order++;
                    leave = prospect.matchesInstruction(lineNumber,
                            insn.getOpcode(), order);
                    break;
            }
        }

        method.instructions.set(insn, new InsnNode(Mutations.mutate(insn.getOpcode())));
    }
}
