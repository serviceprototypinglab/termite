package ch.zhaw.splab.podilizerproc.depdencies;

import java.util.*;

public class JavaPackageInfo {

    private final Set<JavaPackageInfo> subpackages = new HashSet<>();
    private final Set<CompilationUnitInfo> compilationUnitInfos = new HashSet<>();

    private final String name;

    public JavaPackageInfo(String name) {
        this.name = name;
    }

    public void addCompilationUnit(List<String> parentPackages, CompilationUnitInfo cuInfo) {
        if (parentPackages.isEmpty()) {
            compilationUnitInfos.add(cuInfo);
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

    /**
     * @return A set of all package infos which the package depends on.
     */
    public Set<JavaPackageInfo> getRelevantDependencies(JavaPackageInfo rootPckg) {

        HashSet<JavaPackageInfo> allRelevantPackageInfos = new HashSet<>();

        addDependenciesToSet(rootPckg, allRelevantPackageInfos);

        return allRelevantPackageInfos;
    }

    public Set<CompilationUnitInfo> getCompilationUnits() {
        return compilationUnitInfos;
    }

    private void addDependenciesToSet(JavaPackageInfo rootPckg, Set<JavaPackageInfo> relevantDependencies) {
        if (relevantDependencies.contains(this)) {
            return;
        }
        // Add itself first, to avoid infinite recursion
        relevantDependencies.add(this);
        // Add all the dependencies which the contained CUs rely on
        compilationUnitInfos
                .stream()
                .flatMap(cuInfo -> cuInfo.getAllImportedPackages().stream())
                .distinct()
                .map(rootPckg::findPackageInfo)
                .filter(Objects::nonNull)
                .forEach((relevantPckg) -> relevantPckg.addDependenciesToSet(rootPckg, relevantDependencies));
    }

    public JavaPackageInfo findPackageInfo(String completePackageName) {
        return findPackageInfo(Arrays.asList(completePackageName.split("\\.")));
    }

    public JavaPackageInfo findPackageInfo(List<String> pckgNames) {
        if (pckgNames.isEmpty()) {
            return this;
        }
        Optional<JavaPackageInfo> subPckg = subpackages.stream()
                .filter(subpckg -> pckgNames.get(0).equals(subpckg.name))
                .findAny();

        return subPckg
                .map(pckg ->
                        pckg.findPackageInfo(pckgNames.subList(1, pckgNames.size())))
                .orElse(null);
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
