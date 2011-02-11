/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.psychedelic.base;

import org.objectweb.asm.Opcodes;

/**
 *
 * @author sunye
 */
public class Mutations {
        static int[] replacements;

        static {
            initializeReplacements();
        }

        private static void initializeReplacements() {
            replacements = new int[255];
            for (int i = 0; i < replacements.length; i++) {
                replacements[i] = i;
            }
            replacements[Opcodes.LADD] = Opcodes.LSUB;
            replacements[Opcodes.FADD] = Opcodes.FSUB;
            replacements[Opcodes.DADD] = Opcodes.DSUB;
            replacements[Opcodes.IADD] = Opcodes.ISUB;
            replacements[Opcodes.LADD] = Opcodes.LSUB;


            replacements[Opcodes.ISUB] = Opcodes.IADD;
            replacements[Opcodes.LSUB] = Opcodes.LADD;
            replacements[Opcodes.FSUB] = Opcodes.FADD;
            replacements[Opcodes.DSUB] = Opcodes.DADD;
                    
            replacements[Opcodes.IMUL] = Opcodes.IADD;
            replacements[Opcodes.LMUL] = Opcodes.LADD;
            replacements[Opcodes.FMUL] = Opcodes.FADD;
            replacements[Opcodes.DMUL] = Opcodes.DADD;
            
            replacements[Opcodes.IDIV] = Opcodes.IMUL;
            replacements[Opcodes.LDIV] = Opcodes.LMUL;
            replacements[Opcodes.FDIV] = Opcodes.FMUL;
            replacements[Opcodes.DDIV] = Opcodes.DMUL;
            
            replacements[Opcodes.IREM] = Opcodes.IDIV;
            replacements[Opcodes.LREM] = Opcodes.LDIV;
            replacements[Opcodes.FREM] = Opcodes.FDIV;
            replacements[Opcodes.DREM] = Opcodes.DDIV;
            
            replacements[Opcodes.ISHL] = Opcodes.ISHR;
            replacements[Opcodes.LSHL] = Opcodes.LSHR;
            replacements[Opcodes.ISHR] = Opcodes.ISHL;
            replacements[Opcodes.LSHR] = Opcodes.LSHL;
            replacements[Opcodes.IUSHR] = Opcodes.ISHR;
            
            replacements[Opcodes.LUSHR] = Opcodes.LSHR;
            replacements[Opcodes.IAND] = Opcodes.IOR;
            replacements[Opcodes.LAND] = Opcodes.LOR;
            replacements[Opcodes.IOR] = Opcodes.IAND;
            replacements[Opcodes.IXOR] = Opcodes.IOR;

            replacements[Opcodes.LOR] = Opcodes.LAND;
            replacements[Opcodes.LXOR] = Opcodes.LOR;

            replacements[Opcodes.INEG] = Opcodes.NOP;
            replacements[Opcodes.LNEG] = Opcodes.NOP;
            replacements[Opcodes.FNEG] = Opcodes.NOP;
            replacements[Opcodes.DNEG] = Opcodes.NOP;


            // True <-> False
            replacements[Opcodes.ICONST_0] = Opcodes.ICONST_1;
            replacements[Opcodes.ICONST_1] = Opcodes.ICONST_0;


        }

        public static int mutate(int i) {
            return replacements[i];
        }

        public static boolean canMutate(int i) {
            return i != replacements[i];
        }
}
