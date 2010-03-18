/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import java.io.File;
import java.io.PrintStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.types.Path;

/**
 *
 * @author sunye
 */
public class Main {

    private static String COORDINATOR = "fr.inria.peerunit.CoordinatorRunner";
    private static String TESTER = "fr.inria.peerunit.TestRunner";
    private static String PATH = "/Users/sunye/Work/gforge/peerunit/trunk/Benchmarks/Benchmark-1.0.jar";
    private static String TEST = "test.MainTest";

    private Project project;
    private Path path;

    public Main() {
        System.out.println(System.getProperty("user.dir"));
        project = new Project();
        project.setBaseDir(new File(System.getProperty("user.dir")));
        project.init();

      DefaultLogger logger = new DefaultLogger();
		project.addBuildListener(logger);
		logger.setOutputPrintStream(System.out);
		logger.setErrorPrintStream(System.err);
		logger.setMessageOutputLevel(Project.MSG_INFO);

        path = new Path(project, PATH);

        System.out.println("classpath = " + path);
    }

    public Task newCoordinatorTask(Task owner) {
        /*
        Echo echo = new Echo();
        echo.setProject(project);
        echo.setTaskName("MyEcho");
        echo.addText("My Echo");

        return echo;
         *
         */

        Java javaTask = new Java(owner);
        javaTask.setTaskName("runCoordinator");
        javaTask.setProject(project);
        javaTask.setFork(true);
        javaTask.setFailonerror(true);
        javaTask.setClassname(COORDINATOR);


        javaTask.setClasspath(path);
        javaTask.init();

        return javaTask;
    }

    public Task newTesterTask(Task owner) {
        /*
        Echo echo = new Echo();
        echo.setProject(project);
        echo.setTaskName("MyEcho");
        echo.addText("My Echo");
        
        return echo;
        */

        
        Java javaTask = new Java();
        javaTask.setTaskName("runTester");
        javaTask.setProject(project);
        javaTask.setFork(true);
        javaTask.setFailonerror(true);
        javaTask.setClassname(TESTER);
        javaTask.setArgs(TEST);

        javaTask.setClasspath(path);
        javaTask.init();

        return javaTask;

    }

    public void run() {
        System.out.println("run()");
        Parallel par = (Parallel) project.createTask("parallel");

        for (int i = 0; i <= 4 ; i++) {
            Task t = this.newTesterTask(par);
            System.out.println("Adding task: " +  t.getTaskName());
            par.addTask(t);
        }
        
        par.addTask(this.newCoordinatorTask(par));

        
        System.out.println("Ready to execute //");

        par.execute();

    }

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }
}
