package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Filer {
    CompilationUnitTree cu;
    ClassTree clazz;
    MethodTree method;

    public Filer(CompilationUnitTree cu, ClassTree clazz, MethodTree method) {
        this.cu = cu;
        this.clazz = clazz;
        this.method = method;
    }

    /**
     * Creates directory tree for certain lambda function
     * @return true if tree created and false if not
     */
    public boolean createDirectories(){
        File directories = new File(getPath().toString());
        return directories.mkdirs();
    }

    /**
     * Generates path for certain lambda function based on package name, class name, method name and parameters number
     * @return {@link Path} of generated path
     */
    public Path getPath(){
        String packageName;
        String clazzName;
        String methodSpec;

        if (cu.getPackageName() == null){
            packageName =  "aws";
        } else {
            packageName = "aws/" + cu.getPackageName().toString().replace(',', '/');
        }

        clazzName = clazz.getSimpleName().toString();
        int params;
        if (method.getParameters() == null){
            params = 0;
        } else {
            params = method.getParameters().size();
        }
        methodSpec = method.getName().toString() + params;

        return Paths.get(packageName + "/" + clazzName + "/" + methodSpec);
    }
}
