package ch.zhaw.splab.podilizerproc.depdencies;

import javax.tools.JavaFileObject;
import java.util.*;

public class CompilationUnitInfo {

    private final String name;
    private final String packageName;
    private final Set<String> importedPackages;
    private final JavaFileObject sourceFile;

    public CompilationUnitInfo(String name, String packageName, Set<String> importedPackages, JavaFileObject sourceFile) {
        this.name = name;
        this.packageName = packageName;
        this.importedPackages = new HashSet<>(importedPackages);
        this.sourceFile = sourceFile;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<String> getAllImportedPackages() {
        return importedPackages;
    }

    public JavaFileObject getSourceFile() {
        return sourceFile;
    }

    @Override
    public boolean equals(Object o) {
        // Note: The equals method relies on the name being unique to the contained package
        // This is not really clean oop, but it is way easier than comparing the imports every time.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompilationUnitInfo that = (CompilationUnitInfo) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
