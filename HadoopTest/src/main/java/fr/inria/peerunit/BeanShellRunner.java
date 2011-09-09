/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.hdfs.BenchmarkThroughput;

/**
 *
 * @author michel
 */
public class BeanShellRunner {


    private void uniqueInterpreter() throws FileNotFoundException {
        Interpreter interpreter = new Interpreter();

       // interpreter.source("src/main/java/fr/inria/peerunit/teste.bsh");

        FileReader reader = new FileReader("src/main/java/fr/inria/peerunit/teste.bsh");
        try {
            interpreter.eval(reader);
        }  catch (EvalError ee) {
            // Debugger
            System.out.println("Error found on " + ee.getErrorSourceFile()
                    + " at line number " + ee.getErrorLineNumber() + ": "+ee.getMessage());
        }
    }

    private void multipleInterpreters() throws InterruptedException {

        Thread thread = new Thread() {

            public void run() {
                Interpreter interpreter = new Interpreter();

                try {
                    FileReader reader = new FileReader("src/main/java/fr/inria/peerunit/teste.bsh");
                    interpreter.eval(reader);
                }  catch (EvalError ee) {
                    // Debugger
                    System.out.println("Error found on " + ee.getErrorSourceFile()
                        + " at line number " + ee.getErrorLineNumber() + ": "+ee.getMessage());
                } catch (FileNotFoundException fnfe) {
                    
                }
            }

        };

       Thread thread1 = new Thread() {

            public void run() {
                Interpreter interpreter1 = new Interpreter();

                try {
                    FileReader reader = new FileReader("src/main/java/fr/inria/peerunit/teste1.bsh");
                    interpreter1.eval(reader);
                }  catch (EvalError ee) {
                    // Debugger
                    System.out.println("Error found on " + ee.getErrorSourceFile()
                        + " at line number " + ee.getErrorLineNumber() + ": "+ee.getMessage());
                } catch (FileNotFoundException fnfe) {

                }
            }

        };

        thread.start();
        thread.sleep(1000);

        thread1.start();
        thread1.sleep(1000);
    }

    public static void main(String args[]) throws FileNotFoundException, InterruptedException {

        BeanShellRunner runner = new BeanShellRunner();
        runner.multipleInterpreters();
         
    }
}
