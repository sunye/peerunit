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
import java.util.ArrayList;

/**
 * 
 *
 */
public class App {

   // private static String source = "target/classes/fr/inria/psychedelic/sample/PanisCircenses.class";
   // private static String target = "target/mutants/";
	
	private static String source;
	private static String target;
	private static ArrayList outputList = new ArrayList();
        private static String mutClass;
	
	public void runMutation(String src, String targ, String mClass) throws FileNotFoundException, IOException {
		
		source = src;
		target = targ;
                mutClass = mClass;
		
		// Get class for mutation
		//String[] tokens = source.split("/");
		//String mutClass = tokens[tokens.length - 1];
		
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
           
            //File outDir = new File(dir.getAbsolutePath()+"/"+i+++"/");
            File outDir = new File(dir.getPath()+"/"+ i +"/");
            outDir.mkdirs();
            
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dir.getPath()+"/"+ i +"/"+mutClass));
            Psychedelic.applyProspect(input, out, each);
            outputList.add(dir.getPath()+"/"+ i +"/"+mutClass);
            out.close();
            input.close();
            
            System.out.println(each);
            
            i++;
        }        
		
	}
	
	public ArrayList getOutputList() {
		
		return outputList;
	
	}
	
	public static void main(String[] args) {
	
                if (args.length < 3) {
                    System.out.println("Please use arguments: <FileToMut> <MutDestination> <ClassName>");
                    System.exit(0);
                }

		try {
			App mutationApp = new App();
	    	mutationApp.runMutation(args[0], args[1], args[2]);
	    	System.out.println("Mutations successfully generated!");
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.toString());
		} catch (IOException ie) {
			System.out.println(ie.toString());
		}
		
	}

}
