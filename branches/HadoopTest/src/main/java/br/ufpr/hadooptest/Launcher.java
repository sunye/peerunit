/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpr.hadooptest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;

import java.io.PrintStream;

/**
 *
 * @author jeugenio
 */
public class Launcher {
    public static int launchApplication(Class mainClass, String args) {
        int returnCode;

        Project project = new Project();

        project.setBasedir(System.getProperty("user.dir"));
        project.init();

        PrintStream out = System.out;
        PrintStream err = System.err;

        BuildLogger logger = new DefaultLogger();
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);
        logger.setMessageOutputLevel(Project.MSG_INFO);

        project.addBuildListener(logger);

        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));

        project.fireBuildStarted();

        Throwable caught = null;
        try {
            project.log("Launch Application");

            Java javaTask = new Java();
            javaTask.setTaskName("Application Launcher");
            //javaTask.setProject(project);
            javaTask.setFork(true);
            javaTask.setFailonerror(true);
            javaTask.setCloneVm(true);
            javaTask.setClassname(mainClass.getName());
            javaTask.setArgs(args);
            System.out.println("commandline="+javaTask.getCommandLine().toString());
            javaTask.init();

            returnCode = javaTask.executeJava();
        } catch (BuildException e) {
            caught = e;

            returnCode = -1;
        }

        project.fireBuildFinished(caught);

        System.setOut(out);
        System.setErr(err);

        return returnCode;
    }
    public static void main(String[] args){
        System.out.println("teste");
    }
}
