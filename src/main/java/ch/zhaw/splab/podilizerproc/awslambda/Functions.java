package ch.zhaw.splab.podilizerproc.awslambda;

import ch.zhaw.splab.podilizerproc.depdencies.CompilationUnitInfo;
import org.codehaus.plexus.util.FileUtils;

import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

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

                writeRequiredCompilationUnits(path, function);

                ClassLoader cl = getClass().getClassLoader();
                URLClassLoader urlcl = (URLClassLoader)cl;
                URL[] classPath = urlcl.getURLs();

                PomGenerator pomGenerator = new PomGenerator(function.getAwsFiler().getPomPath(), classPath);
                pomGenerator.create();

                JarBuilder jarBuilder = new JarBuilder(function.getAwsFiler().getPomPath().toString());

                jarBuilder.mvnBuild();

                String lamdaJarLocation = function.getAwsFiler().getPomPath().toString();
                lamdaJarLocation = lamdaJarLocation.replace('\\', '/') + "/target/lambda-java-1.0-SNAPSHOT.jar";

                System.out.println("!!!!!!!!!!! Skipping upload !!!!!!!!!!!!!");
                /* TODO:
                JarUploader jarUploader = new JarUploader(function.getLambdaFunctionName(), lamdaJarLocation, "LambdaFunction::handleRequest",
                        function.getLambdaAnnotation().region(), function.getLambdaAnnotation().timeOut(),
                        function.getLambdaAnnotation().memorySize(), function.getLambdaAnnotation().endPoint());
                jarUploader.uploadFunction(); */
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeRequiredCompilationUnits(String path, LambdaFunction function) {
        Set<CompilationUnitInfo> requiredCompilationUnits = function.getRequiredCompilationUnits();
        for (CompilationUnitInfo requiredCompilationUnit : requiredCompilationUnits) {
            String packageName = requiredCompilationUnit.getPackageName();
            String absoluteFilePath = path + "/" + packageName.replace('.', '/');
            JavaFileObject sourceFile = requiredCompilationUnit.getSourceFile();

            File targetFile = new File(absoluteFilePath);

            try(Reader reader = sourceFile.openReader(true);
                BufferedReader bufferedReader = new BufferedReader(reader);
                PrintWriter writer = new PrintWriter(targetFile)) {
                String nextLine;
                while ((nextLine = bufferedReader.readLine()) != null) {
                    writer.println(nextLine);
                }
            } catch (IOException e) {
                System.out.println("[TERMITE] Failed to copy required compilation unit to " + absoluteFilePath);
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
