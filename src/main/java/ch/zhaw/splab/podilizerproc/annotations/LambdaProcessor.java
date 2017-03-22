package ch.zhaw.splab.podilizerproc.annotations;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Processor of {@link Lambda} annotation
 */
@SupportedAnnotationTypes({"ch.zhaw.splab.podilizerproc.annotations.Lambda"})
public class LambdaProcessor extends AbstractProcessor {
    private Trees trees;

    /**
     * Initialization of {@link ProcessEnvironment} object and {@ling Trees} object
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(Lambda.class);
        Messager messager = processingEnv.getMessager();
        MethodScanner visitor = new MethodScanner();
        messager.printMessage(Diagnostic.Kind.NOTE, "Lambda annotations: " + annotatedMethods.size());
        for (Element element :
                annotatedMethods) {
            messager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName());
            TreePath tp = trees.getPath(element);
            visitor.scan(tp, trees);
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "There are methods\n" +
                Arrays.toString(visitor.getMethods().toArray()));
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Method visitor
     */
    private class MethodScanner extends TreePathScanner {
        private List<MethodTree> methods = new ArrayList<>();

        @Override
        public Object visitMethod(MethodTree methodTree, Object o) {
            methods.add(methodTree);
            return null;
        }

        public List<MethodTree> getMethods() {
            return methods;
        }
    }
}
