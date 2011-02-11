/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.psychedelic;

import fr.inria.psychedelic.gathering.ProspectGatheringClassAdapter;
import fr.inria.psychedelic.base.Prospect;
import fr.inria.psychedelic.gathering.ClassMutator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 *
 * @author sunye
 */
public class Psychedelic {

    public static List<Prospect> prospectsFor(InputStream is) throws IOException {
        
        ClassReader reader = new ClassReader(is);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new CheckClassAdapter(writer);
        ProspectGatheringClassAdapter pg = new ProspectGatheringClassAdapter(visitor);
        reader.accept(pg, ClassReader.SKIP_FRAMES);

        return pg.getProspects();
    }

    public static void applyProspect(InputStream is, OutputStream os, Prospect p) throws IOException {
        ClassMutator mutator = new ClassMutator();
        ClassReader reader = new ClassReader(is);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.SKIP_FRAMES);

        mutator.apply(node, p);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);

        byte[] byteArray = writer.toByteArray();
        os.write(byteArray);
        os.flush();
    }

}
