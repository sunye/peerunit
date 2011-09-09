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

    private void multipleInterpreters() {
        
    }

    public static void main(String args[]) throws FileNotFoundException {

       BeanShellRunner runner = new BeanShellRunner();

       runner.uniqueInterpreter();
         
    }
}
