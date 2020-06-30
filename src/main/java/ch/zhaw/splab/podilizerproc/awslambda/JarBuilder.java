package ch.zhaw.splab.podilizerproc.awslambda;

import ch.zhaw.splab.podilizerproc.statistic.Timer;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Contains tools for creating proper result project tree ind building
 */
public class JarBuilder {
    private String path;


    public JarBuilder() {
    }

    public JarBuilder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Builds the maven project using maven sdk for java
     *
     * @throws URISyntaxException
     */
    public String mvnBuild() {        Timer.start();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(path));
        request.setGoals(Arrays.asList("clean", "install"));
        File buildLog = new File(path + "/buildLog.txt");

        Invoker invoker = new DefaultInvoker();
        try {
            if (invoker.getMavenHome() == null) {
                String mavenHome = System.getenv("MAVEN_HOME");
                if (mavenHome == null || mavenHome.isEmpty()) {
                    mavenHome = "/usr/share/maven/";
                }
                invoker.setMavenHome(new File(mavenHome));
            }
            //log the build output to file
            PrintStream printStream = new PrintStream(buildLog);
            InvocationOutputHandler outputHandler = new PrintStreamHandler(printStream, true);
            invoker.setOutputHandler(outputHandler);

            InvocationResult result = invoker.execute(request);
            Timer.stop();
            printBuildResult(path, result.getExitCode());
            System.out.println("[TERMITE] Project " + path + " has been built in " + Timer.getFormatedTime());
            printStream.close();
            if (result.getExitCode() == 0) {
                return path;
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printBuildResult(String path, int exitCode) {
        String result = "[TERMITE] Build result of project " + path + " : ";
        if (exitCode == 0) {
            result += "[SUCCESS]";
        } else {
            result += "[FAILURE]";
        }
        System.out.println(result);
    }
}

