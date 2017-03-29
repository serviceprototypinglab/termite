package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;

public class LambdaFunction {
    MethodTree method;
    ClassTree clazz;
    CompilationUnitTree cu;

    public LambdaFunction(MethodTree method, ClassTree clazz, CompilationUnitTree cu) {
        this.method = method;
        this.clazz = clazz;
        this.cu = cu;
    }

    @Override
    public String toString() {
        return "LambdaFunction{" +
                "method=" + method +
                ", clazz=" + clazz +
                ", cu=" + cu +
                '}';
    }
}
