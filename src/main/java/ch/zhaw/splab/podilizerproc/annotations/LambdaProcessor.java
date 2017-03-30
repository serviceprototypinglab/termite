package ch.zhaw.splab.podilizerproc.annotations;

import ch.zhaw.splab.podilizerproc.awslambda.LambdaFunction;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.classfile.AccessFlags;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Processor of {@link Lambda} annotation
 */
@SupportedAnnotationTypes({"ch.zhaw.splab.podilizerproc.annotations.Lambda"})
public class LambdaProcessor extends AbstractProcessor {
    private Trees trees;

    /**
     * Initialization of {@link ProcessEnvironment} object and {@link Trees} object
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        List<MethodTree> methods = new ArrayList<>();
        List<ClassTree> classes = new ArrayList<>();
        List<CompilationUnitTree> cuList = new ArrayList<>();

        List<LambdaFunction> functions = new ArrayList<>();

        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(Lambda.class);
        if (annotatedMethods.size() == 0){
            return true;
        }
        for (Element element :
                annotatedMethods) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "Enclosed element of " + element.getSimpleName() + "" +
//                    " is " + element.getEnclosingElement().getSimpleName() + ". GrandParent is " +
//                    element.getEnclosingElement().getEnclosingElement().getKind());

            MethodScanner methodScanner = new MethodScanner();
            TypeScanner typeScanner = new TypeScanner();
            CUVisitor cuVisitor = new CUVisitor();

            TreePath tp = trees.getPath(element);
            methodScanner.scan(tp, trees);
            TreePath ctp = trees.getPath(getMostExternalType(element));
            typeScanner.scan(ctp, trees);
            TreePath tp1 = trees.getPath(getMostExternalType(element));
            cuVisitor.visit(tp1, trees);
//            for (Tree tree :
//                    typeScanner.getClazz().getMembers()) {
//                messager.printMessage(Diagnostic.Kind.NOTE,tree.toString()  + " has type " + tree.getKind().toString() + "\n");
//            }
            //messager.printMessage(Diagnostic.Kind.NOTE, "cu " + cuVisitor.getCu().getImports());

            functions.add(new LambdaFunction(methodScanner.getMethod(), typeScanner.getClazz(), cuVisitor.getCu()));
        }
        //messager.printMessage(Diagnostic.Kind.NOTE, "Annotated methods: " + functions.size());
        for (LambdaFunction function :
                functions) {
            //messager.printMessage(Diagnostic.Kind.NOTE, "F: " + function.imports());
            try {
                File file  = new File("aws.java");
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.print(function.create());
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        for (CompilationUnitTree cu:
//             cuList) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "cu " + cu.getPackageName() + " - imports - \n" +
//                    cu.getImports());
//        }
//        //messager.printMessage(Diagnostic.Kind.NOTE, "Classes are " + Arrays.toString(classes.toArray()));
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
    private class CUVisitor extends SimpleTreeVisitor{
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
     * @param element is {@link Element} object to find external class of
     * @return {@link TypeElement} object of the most external type
     */
    private TypeElement getMostExternalType(Element element){
        if (element.getKind().isClass() & !element.getEnclosingElement().getKind().isClass()){
            return (TypeElement)element;
        }
        Element parent = element;
        Element grandParent;
        do {
            parent = parent.getEnclosingElement();
            grandParent = parent.getEnclosingElement();
        } while (!(parent.getKind().isClass() & !grandParent.getKind().isClass() &
        !grandParent.getKind().isInterface()));
        return (TypeElement)parent;

    }

    // TODO: 3/28/17 recreate getting of external libraries(include maven dependencies)
    private void writeExternalCP(){
        ClassLoader cl = getClass().getClassLoader();
        URLClassLoader urlcl = (URLClassLoader)cl;
        URL[] classPath = urlcl.getURLs();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("/home/dord/pathsExternal.txt");
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
