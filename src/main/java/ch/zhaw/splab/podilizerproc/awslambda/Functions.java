package ch.zhaw.splab.podilizerproc.awslambda;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Functions {
    List<LambdaFunction> functions;

    public Functions(List<LambdaFunction> functions) {
        this.functions = functions;
    }

    public void write(){
        for (LambdaFunction function :
                functions) {
            try {
                String path = function.awsFiler.getPath().toString();
                function.awsFiler.createDirectories();
                File file  = new File(path + "/aws.java");
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.print(function.create());
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
