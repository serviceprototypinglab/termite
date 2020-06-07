package ch.zhaw.splab.podilizerproc.depdencies;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CompilationUnitInfo {

    private final String name;
    private final List<String> allImports = new ArrayList<>();
    private JavaFileObject sourceFile = null;
    // List of impports
    // Src file reference

    public CompilationUnitInfo(String name) {
        this.name = name;
    }

    public void addImport(String importName) {
        allImports.add(importName);
    }

    public void addImports(List<String> importNames) {
        allImports.addAll(importNames);
    }

    public void setSourceFile(JavaFileObject sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getName() {
        return name;
    }

    public List<String> getAllImports() {
        return allImports;
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
