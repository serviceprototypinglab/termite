package ch.zhaw.splab.podilizerproc.awslambda;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class Functions {
    List<LambdaFunction> functions;

    public Functions(List<LambdaFunction> functions) {
        this.functions = functions;
    }

    /**
     * Writes functions and support classes into files
     */
    public void write(){
        try {
            FileUtils.deleteDirectory("aws");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (LambdaFunction function :
                functions) {
            try {
                String path = function.getAwsFiler().getPath().toString();
                function.getAwsFiler().createDirectories();
                File file  = new File(path + "/LambdaFunction.java");
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.print(function.create());
                printWriter.close();

                File input = new File(path + "/InputType.java");
                PrintWriter printWriter1 = new PrintWriter(input);
                printWriter1.print(function.createInputType());
                printWriter1.close();

                File output = new File(path + "/OutputType.java");
                PrintWriter printWriter2 =new PrintWriter(output);
                printWriter2.print(function.createOutputType());
                printWriter2.close();

                ClassLoader cl = getClass().getClassLoader();
                URLClassLoader urlcl = (URLClassLoader)cl;
                URL[] classPath = urlcl.getURLs();

                PomGenerator pomGenerator = new PomGenerator(function.getAwsFiler().getPomPath(), classPath);
                pomGenerator.create();

                JarBuilder jarBuilder = new JarBuilder(function.getAwsFiler().getPomPath().toString());

                jarBuilder.mvnBuild();

                JarUploader jarUploader = new JarUploader(function.getLambdaFunctionName(),
                        function.getAwsFiler().getPomPath().toString() + "/target/lambda-java-1.0-SNAPSHOT.jar",
                        "LambdaFunction::handleRequest",
                        function.getLamdaAnnotation().region(), function.getLamdaAnnotation().role(),
                        function.getLamdaAnnotation().timeOut(), function.getLamdaAnnotation().memorySize());
                jarUploader.uploadFunction();



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    // TODO: 3/28/17 recreate getting of external libraries(include maven dependencies)
    private void writeExternalCP(String pathOut){
        ClassLoader cl = getClass().getClassLoader();
        URLClassLoader urlcl = (URLClassLoader)cl;
        URL[] classPath = urlcl.getURLs();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(pathOut + "cp.txt");
            for (URL path :
                    classPath) {
                fileWriter.write(path + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
