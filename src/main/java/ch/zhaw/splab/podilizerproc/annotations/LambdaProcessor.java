package ch.zhaw.splab.podilizerproc.annotations;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.FileWriter;
import java.io.IOException;
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
        MethodScanner methodScanner = new MethodScanner();
        TypeScanner typeScanner = new TypeScanner();

        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(Lambda.class);
        if (annotatedMethods.size() == 0){
            return true;
        }
        for (Element element :
                annotatedMethods) {
            messager.printMessage(Diagnostic.Kind.NOTE, "" + element.getSimpleName() + "'s most external parent is " +
                    getMostExternalType(element).getSimpleName());
//            messager.printMessage(Diagnostic.Kind.NOTE, "Enclosed element of " + element.getSimpleName() + "" +
//                    " is " + element.getEnclosingElement().getSimpleName() + ". GrandParent is " +
//                    element.getEnclosingElement().getEnclosingElement().getKind());
            TreePath tp = trees.getPath(element);
            methodScanner.scan(tp, trees);
            TreePath ctp = trees.getPath(getMostExternalType(element));
            typeScanner.scan(ctp, trees);
        }
        methods.addAll(methodScanner.getMethods());
        classes.addAll(typeScanner.getClasses());
        messager.printMessage(Diagnostic.Kind.NOTE, "Classes are " + Arrays.toString(classes.toArray()));
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

        private List<MethodTree> getMethods() {
            return methods;
        }
    }

    /**
     * Class visitor
     */
    private class TypeScanner extends TreePathScanner {
        private List<ClassTree> classes = new ArrayList<>();
        @Override
        public Object visitClass(ClassTree classTree, Object o) {
            classes.add(classTree);
            return null;
        }

        public List<ClassTree> getClasses() {
            return classes;
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
