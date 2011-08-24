package fr.inria.psychedelic.base;

import fr.inria.psychedelic.Psychedelic;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class MultiClass {

    private static final ArrayList<String> outputList = new ArrayList<String>();

    /**
     * Method to create mutation of classes within a directory.
     *
     * @param source The source directory containing the classes.
     * @param target The target directory to hold the mutations.
     * @throws java.io.IOException The exception.
     */
    void runMutation(String source, String target) throws IOException {

        File sourceDir = new File(source);
        File[] classes = sourceDir.listFiles();

        int i = 0;
        for (File clazz : classes) {
            if (clazz.getName().endsWith(".class")) {

                /**
                 * Step 1 - Generate prospects
                 * Prospects are generated for each class inside the directory
                 */
                List<Prospect> prospects;
                FileInputStream input = new FileInputStream(clazz);
                prospects = Psychedelic.prospectsFor(input);
                input.close();

                /**
                 * Step 2 - Apply prospects
                 * Each prospect results in a new class
                 */
                File dir = new File(target);
                dir.mkdirs();

                for (Prospect prospect : prospects) {

                    input = new FileInputStream(clazz);

                    File outDir = new File(dir.getPath() + "/" + i + "/");
                    outDir.mkdirs();

                    // Copy the other classes to the output directory
                    copyRemainingFiles(clazz, classes, dir, i);

                    OutputStream out = new BufferedOutputStream(new FileOutputStream(dir.getPath() + "/" + i + "/" + clazz.getName()));
                    Psychedelic.applyProspect(input, out, prospect);
                    outputList.add(dir.getPath() + "/" + i + "/");
                    out.close();
                    input.close();

                    System.out.println(prospect);
                    i++;
                }
            }
        }
    }

    /**
     * Helper method to copy unaltered classes from source directory to the mutant directory.
     *
     * @param file      The mutated file.
     * @param files     Vector of all files within the source directory.
     * @param directory The root mutants directory.
     * @param idx       The index of current mutation.
     * @throws java.io.IOException The exception.
     */
    private void copyRemainingFiles(File file, File[] files, File directory, int idx) throws IOException {
        for (File other : files) {
            if (other != file) {
                FileChannel source = null;
                FileChannel destination = null;
                try {
                    source = new FileInputStream(other).getChannel();
                    destination = new FileOutputStream(directory.getPath() + "/" + idx + "/" + other.getName()).getChannel();
                    destination.transferFrom(source, 0, source.size());
                } finally {
                    if (source != null) {
                        source.close();
                    }
                    if (destination != null) {
                        destination.close();
                    }
                }
            }
        }
    }

    /**
     * Helper method to create a jar file from files within a directory.
     * <p/>
     * It deletes all classes within the mutant directory after jar creation.
     *
     * @param name The name for the jar file.
     * @throws java.io.IOException The exception.
     */
    private void createJarFiles(String name) throws IOException {
        for (String mutant : outputList) {
            FileOutputStream stream = new FileOutputStream(mutant + name + ".jar");
            JarOutputStream target = new JarOutputStream(stream, new Manifest());

            File[] files = new File(mutant).listFiles();

            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    JarEntry jarAdd = new JarEntry(file.getName());
                    jarAdd.setTime(file.lastModified());
                    target.putNextEntry(jarAdd);

                    FileInputStream in = new FileInputStream(file);
                    byte buffer[] = new byte[10240];
                    while (true) {
                        int read = in.read(buffer, 0, buffer.length);
                        if (read <= 0)
                            break;
                        target.write(buffer, 0, read);
                    }
                    in.close();
                }
            }
            target.close();
            stream.close();

            for (File file : files) {
                if (file.getName().endsWith(".class"))
                    file.delete();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar Psychedelic.jar source-directory destination jar-name");
            System.exit(1);
        }

        try {
            MultiClass mutationApp = new MultiClass();
            mutationApp.runMutation(args[0], args[1]);
            mutationApp.createJarFiles(args[2]);
            System.out.println("Mutations successfully generated!");
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe.toString());
        } catch (IOException ie) {
            System.out.println(ie.toString());
        }
    }
}