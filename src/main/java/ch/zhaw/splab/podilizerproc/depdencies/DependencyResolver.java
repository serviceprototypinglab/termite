package ch.zhaw.splab.podilizerproc.depdencies;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyResolver {

    private Trees trees;
    RoundEnvironment roundEnv;

    private final JavaPackageInfo rootPckg = new JavaPackageInfo("");

    public DependencyResolver(Trees trees, RoundEnvironment roundEnv) {
        this.trees = trees;
        this.roundEnv = roundEnv;
        loadCompleteSourceInformation();
    }

    /**
     * This method is responsible for loading the general package structure of the project.
     * This will later be used to get compilation units which are related to each other.
     */
    public void loadCompleteSourceInformation() {
        for (Element rootElement : roundEnv.getRootElements()) {
            // TODO: Build a strucutre which keeps track of all dependencies a element has
            // TODO: This might fail if there is incredemental processing
            //  https://stackoverflow.com/questions/18038514/annotation-processor-only-processing-a-modified-class-after-first-run
            TreePath rootElemPath = trees.getPath(rootElement);

            CUVisitor rootVisistor = new CUVisitor();
            rootVisistor.visit(rootElemPath, trees);
            CompilationUnitTree cu = rootVisistor.getCu();

            String pckgName = cu.getPackageName() == null? "": cu.getPackageName().toString();
            String srcName = new File(cu.getSourceFile().toUri()).getName();
            srcName = srcName.replace(".java", "");


            Set<String> allImports = cu.getImports().stream()
                    .map(ImportTree::getQualifiedIdentifier)
                    .map(Object::toString)
                    .map(completeImport -> completeImport.substring(0, completeImport.lastIndexOf('.')))
                    .collect(Collectors.toSet());

            CompilationUnitInfo compilationUnitInfo =
                    new CompilationUnitInfo(srcName, pckgName, allImports, cu.getSourceFile());

            List<String> pckNames = Arrays.asList(pckgName.split("\\."));
            rootPckg.addCompilationUnit(pckNames, compilationUnitInfo);
        }
    }

    public Set<CompilationUnitInfo> resolveDependencies(Element element) {
        TreePath elemPath = trees.getPath(element);
        CUVisitor cuVisitor = new CUVisitor();
        cuVisitor.visit(elemPath, trees);
        CompilationUnitTree compilationUnit = cuVisitor.cu;

        // We are using the package name, not the file name itself to determin which files are relevant
        // This is done because currently only the imports are used for the analysis
        // this makes the analysis itself a lot easier, but might come with a small overhead compared
        // to a more complex analysis of all used types in the classes themself
        String packageName = compilationUnit.getPackageName().toString();

        JavaPackageInfo packageInfo = rootPckg.findPackageInfo(packageName);
        if (packageInfo == null) {
            System.out.println("[TERMITE] WARNING: Unable to find package " + packageName);
            return Collections.emptySet();
        } else {
            System.out.println("[TERMITE] Found required dependencies for package " + packageName);
            return packageInfo.getRelevantDependencies(rootPckg)
                    .stream()
                    .map(JavaPackageInfo::getCompilationUnits)
                    .flatMap(Set::stream)
                    .peek(cu -> System.out.println("Adding: " + cu.getSourceFile().getName()))
                    .collect(Collectors.toSet());
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


}
