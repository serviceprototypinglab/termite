package ch.zhaw.splab.podilizerproc.annotations;

import ch.zhaw.splab.podilizerproc.awslambda.Functions;
import ch.zhaw.splab.podilizerproc.awslambda.LambdaFunction;
import ch.zhaw.splab.podilizerproc.depdencies.DependencyResolver;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Processor of {@link Lambda} annotation
 */
@SupportedAnnotationTypes({"ch.zhaw.splab.podilizerproc.annotations.Lambda"})
public class LambdaProcessor extends AbstractProcessor {
    private Trees trees;
    private Types typeUtils;

    /**
     * Initialization of {@link ProcessEnvironment} object and {@link Trees} object
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);

        System.out.println("[TERMITE] Annotation Proccessor init.");
        typeUtils = processingEnv.getTypeUtils();

    }


    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<LambdaFunction> functions = new ArrayList<>();

        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(Lambda.class);
        if (annotatedMethods.size() == 0) {
            return true;
        }
        DependencyResolver dependencyResolver = new DependencyResolver(trees, roundEnv);

        for (Element element :
                annotatedMethods) {
            MethodScanner methodScanner = new MethodScanner();
            TypeScanner typeScanner = new TypeScanner();
            CUVisitor cuVisitor = new CUVisitor();


            System.out.println("###############################################################");
            dependencyResolver.resolveDependencies(element);
            System.out.println("###############################################################");


            TreePath tp = trees.getPath(element);
            methodScanner.scan(tp, trees);
            TreePath ctp = trees.getPath(getMostExternalType(element));
            typeScanner.scan(ctp, trees);
            TreePath tp1 = trees.getPath(getMostExternalType(element));
            cuVisitor.visit(tp1, trees);


            Lambda lambda = element.getAnnotation(Lambda.class);
            LambdaFunction lambdaFunction =
                    new LambdaFunction(methodScanner.getMethod(), typeScanner.getClazz(), cuVisitor.getCu(), lambda);
            functions.add(lambdaFunction);

        }
        Functions functionsWriter = new Functions(functions);
        functionsWriter.write();
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
        private MethodTree method = null;

        @Override
        public Object visitMethod(MethodTree methodTree, Object o) {
            methods.add(methodTree);
            method = methodTree;
            return methodTree;
        }

        private List<MethodTree> getMethods() {
            return methods;
        }

        public MethodTree getMethod() {
            return method;
        }
    }

    /**
     * Class visitor
     */
    private class TypeScanner extends TreePathScanner {
        private List<ClassTree> classes = new ArrayList<>();
        private ClassTree clazz = null;

        @Override
        public Object visitClass(ClassTree classTree, Object o) {
            classes.add(classTree);
            clazz = classTree;
            return classTree;
        }

        public List<ClassTree> getClasses() {
            return classes;
        }

        public ClassTree getClazz() {
            return clazz;
        }
    }

    /**
     * Compilation Unit visitor
     */
    private class CUVisitor extends SimpleTreeVisitor {
        private List<CompilationUnitTree> cuList = new ArrayList<>();
        private CompilationUnitTree cu = null;

        @Override
        public Object visitCompilationUnit(CompilationUnitTree compilationUnitTree, Object o) {
            cuList.add(compilationUnitTree);
            cu = compilationUnitTree;
            return null;
        }

        public List<CompilationUnitTree> getCuList() {
            return cuList;
        }

        public CompilationUnitTree getCu() {
            return cu;
        }
    }

    /**
     * Gives the most external class owner of the code structure
     *
     * @param element is {@link Element} object to find external class of
     * @return {@link TypeElement} object of the most external type
     */
    private TypeElement getMostExternalType(Element element) {
        if (element.getKind().isClass() & !element.getEnclosingElement().getKind().isClass()) {
            return (TypeElement) element;
        }
        Element parent = element;
        Element grandParent;
        do {
            parent = parent.getEnclosingElement();
            grandParent = parent.getEnclosingElement();
        } while (!(parent.getKind().isClass() & !grandParent.getKind().isClass() &
                !grandParent.getKind().isInterface()));
        return (TypeElement) parent;

    }


}
