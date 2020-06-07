package ch.zhaw.splab.podilizerproc.depdencies;

import javax.tools.JavaFileObject;
import java.util.*;

public class CompilationUnitInfo {

    private final String name;
    private final Set<String> importedPackages = new HashSet<>();
    private JavaFileObject sourceFile = null;
    // List of impports
    // Src file reference

    public CompilationUnitInfo(String name) {
        this.name = name;
    }

    public void addImport(String importName) {
        importedPackages.add(importName);
    }

    public void addImports(Collection<String> importNames) {
        importedPackages.addAll(importNames);
    }

    public void setSourceFile(JavaFileObject sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getName() {
        return name;
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
