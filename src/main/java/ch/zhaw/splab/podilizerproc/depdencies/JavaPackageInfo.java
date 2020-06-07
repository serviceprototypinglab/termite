package ch.zhaw.splab.podilizerproc.depdencies;

import java.util.*;

public class JavaPackageInfo {

    private final Set<JavaPackageInfo> subpackages = new HashSet<>();
    private final Set<CompilationUnitInfo> compliationUnits = new HashSet<>();

    private final String name;

    public JavaPackageInfo(String name) {
        this.name = name;
    }

    public void addCompilationUnit(List<String> parentPackages, CompilationUnitInfo cuInfo) {
        if (parentPackages.isEmpty()) {
            compliationUnits.add(cuInfo);
        } else {
            String nextPckgName = parentPackages.get(0);
            Optional<JavaPackageInfo> foundPackage = subpackages.stream()
                    .filter(pckgInfo -> nextPckgName.equals(pckgInfo.name))
                    .findFirst();
            JavaPackageInfo nextPackage;
            if (foundPackage.isPresent()) {
                nextPackage = foundPackage.get();
            } else {
                nextPackage = new JavaPackageInfo(nextPckgName);
                subpackages.add(nextPackage);
            }
            nextPackage.addCompilationUnit(parentPackages.subList(1, parentPackages.size()), cuInfo);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaPackageInfo that = (JavaPackageInfo) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
