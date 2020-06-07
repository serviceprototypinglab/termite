package ch.zhaw.splab.podilizerproc.depdencies;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyResolver {

    private Trees trees;
    private Types typeUtils;
    RoundEnvironment roundEnv;

    private final JavaPackageInfo rootPckg = new JavaPackageInfo("");

    public DependencyResolver(Trees trees, Types typeUtils, RoundEnvironment roundEnv) {
        this.trees = trees;
        this.typeUtils = typeUtils;
        this.roundEnv = roundEnv;
        loadCompleteSourceInformation();
    }

    public void loadCompleteSourceInformation() {
        for (Element rootElement : roundEnv.getRootElements()) {
            // TODO: Build a strucutre which keeps track of all dependencies a element has
            // TODO: This might fail if there is incredemental processing
            //  https://stackoverflow.com/questions/18038514/annotation-processor-only-processing-a-modified-class-after-first-run
            TreePath rootElemPath = trees.getPath(rootElement);

            CUVisitor rootVisistor = new CUVisitor();
            rootVisistor.visit(rootElemPath, trees);
            CompilationUnitTree cu = rootVisistor.getCu();

            String pckgName = cu.getPackageName().toString();
            String srcName = new File(cu.getSourceFile().toUri()).getName();
            srcName = srcName.replace(".java", "");

            String canoncialName = pckgName + "." + srcName;
            System.out.println("Completename: " + canoncialName);

            CompilationUnitInfo compilationUnitInfo = new CompilationUnitInfo(srcName);
            compilationUnitInfo.setSourceFile(cu.getSourceFile());
            compilationUnitInfo.addImport(pckgName);
            List<String> allImports = cu.getImports().stream()
                    .map(ImportTree::getQualifiedIdentifier)
                    .map(Object::toString)
                    .peek(importName -> System.out.println("Added import '" + importName + "'"))
                    .collect(Collectors.toList());
            compilationUnitInfo.addImports(allImports);

            List<String> pckNames = Arrays.asList(pckgName.split("\\."));
            rootPckg.addCompilationUnit(pckNames, compilationUnitInfo);
        }
    }

    public Set<String> resolveDependencies(Element element) {
        Set<String> result = new HashSet<>();
        CUVisitor cuVisitor = new CUVisitor();

        TreePath tp = trees.getPath(element);
        TreePath tp1 = trees.getPath(getMostExternalType(element));
        cuVisitor.visit(tp1, trees);

        CompilationUnitTree compilationUnit = cuVisitor.cu;
        JavaFileObject sourceFile = compilationUnit.getSourceFile();

        try {
            System.out.println("src-Content: " + sourceFile.getCharContent(true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Root elems:");
        for (Element rootElement : roundEnv.getRootElements()) {
            // TODO: Build a strucutre which keeps track of all dependencies a element has
            // TODO: This might fail if there is incredemental processing
            //  https://stackoverflow.com/questions/18038514/annotation-processor-only-processing-a-modified-class-after-first-run
            System.out.println("Elem: " + rootElement);
            System.out.println(rootElement.asType());
            TreePath rootElemPath = trees.getPath(rootElement);
            CUVisitor rootVisistor = new CUVisitor();
            rootVisistor.visit(rootElemPath, trees);
            System.out.println(rootVisistor.getCu());

            // Direct Depdencies: List of imports
            // HashMap packageName -> List<Elements>

            // Tree with all packages:
        }


        for (ImportTree anImport : compilationUnit.getImports()) {
            Tree qualifiedIdentifier = anImport.getQualifiedIdentifier();
            System.out.println("Import qualifier: " + qualifiedIdentifier);
            // Options "bla.fasel.myclass"
            // "bla.fasel.*"

            // Element -> Tree
            // Tree -> Imports
            // TypeMirror -> Element

            // Imports -> ? -> Element


        }



        return result;
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
        if (element.getKind().isClass() && !element.getEnclosingElement().getKind().isClass()) {
            return (TypeElement) element;
        }
        Element parent = element;
        Element grandParent;
        do {
            parent = parent.getEnclosingElement();
            grandParent = parent.getEnclosingElement();
        } while (!(parent.getKind().isClass() && !grandParent.getKind().isClass() &&
                !grandParent.getKind().isInterface()));
        return (TypeElement) parent;

    }
}
