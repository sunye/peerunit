package fr.univnantes.alma.rmilite;

import java.io.File;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.junit.Before;

public class RemoteTestSocket extends AbstractRemoteTest {

    static {

	final class RunnableServer implements Runnable {

	    @Override
	    public void run() {

		System.out.println(System.getProperty("user.dir"));
		Project project = new Project();
		project.setBaseDir(new File(System.getProperty("user.dir")));
		project.init();
		DefaultLogger logger = new DefaultLogger();
		project.addBuildListener(logger);
		logger.setOutputPrintStream(System.out);
		logger.setErrorPrintStream(System.err);
		logger.setMessageOutputLevel(Project.MSG_INFO);
		System.setOut(new PrintStream(new DemuxOutputStream(project,
			false)));
		System.setErr(new PrintStream(new DemuxOutputStream(project,
			true)));
		project.fireBuildStarted();

		System.out.println("running");
		Throwable caught = null;
		try {
		    Echo echo = new Echo();
		    echo.setTaskName("Echo");
		    echo.setProject(project);
		    echo.init();
		    echo.setMessage("Launching FakeServer");
		    echo.execute();

		    Java javaTask = new Java();
		    javaTask.setTaskName("runjava");
		    javaTask.setProject(project);
		    javaTask.setFork(true);
		    javaTask.setFailonerror(true);
		    javaTask.setClassname(fr.univnantes.alma.rmilite.FakeServer.class
			    .getName());
		    javaTask.setArgs("20020");
		    Path path = new Path(project, new File(System
			    .getProperty("user.dir")
			    + "/target/test-classes/").getAbsolutePath());
		    path.add(new Path(project, new File(System
			    .getProperty("user.dir")
			    + "/target/classes/").getAbsolutePath()));
		    System.out.println("classpath = " + path);
		    javaTask.setClasspath(path);
		    javaTask.init();
		    int ret = javaTask.executeJava();
		    System.out.println("java task return code: " + ret);
		} catch (BuildException e) {
		    caught = e;
		}
		project.log("finished");
		project.fireBuildFinished(caught);
	    }
	}

	final Thread thread = new Thread(new RunnableServer());
	thread.start();

	try {
	    Thread.sleep(10000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Before
    public void setUp() {
	setConfigManagerStrategy(new ConfigManager_Socket());
	setPort(20020);
    }

}
