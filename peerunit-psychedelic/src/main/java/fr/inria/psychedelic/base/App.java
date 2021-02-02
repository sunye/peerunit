package fr.inria.psychedelic.base;

import fr.inria.psychedelic.Psychedelic;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 
 *
 */
public class App {

    private static String source = "target/classes/fr/inria/psychedelic/sample/PanisCircenses.class";
    private static String target = "target/mutants/";

    public static void main(String[] args) throws FileNotFoundException, IOException {

        // Step 1
        // Generate prospects
        List<Prospect> prospects;
        File f = new File(source);
        FileInputStream input = new FileInputStream(f);
        prospects = Psychedelic.prospectsFor(input);
        input.close();

        // Step 2
        // Apply prospects
        // 1 prospect == 1 .class file.
        File dir = new File(target);
        dir.mkdirs();
        int i = 0;
        for(Prospect each : prospects) {
            input = new FileInputStream(source);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dir.getAbsolutePath()+"/Mutant"+i++ +".class"));
            Psychedelic.applyProspect(input, out, each);
            out.close();
            input.close();
            //System.out.println(each);
        }        
        
        System.out.println("Hello World!");
    }
}
