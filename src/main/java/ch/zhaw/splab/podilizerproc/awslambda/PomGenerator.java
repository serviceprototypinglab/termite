package ch.zhaw.splab.podilizerproc.awslambda;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PomGenerator {
    private Path path;
    private URL[] externalLibs;

    public PomGenerator(Path path, URL[] externalLibs) {
        this.path = path;
        this.externalLibs = externalLibs;
    }

    public void create() {
        Model model = new Model();
        model.setGroupId("doc-examples");
        model.setArtifactId("lambda-java");
        model.setPackaging("jar");
        model.setVersion("1.0-SNAPSHOT");
        model.setName("lambda-java-example");
        List<Dependency> allDependencies = dependencies();
        allDependencies.addAll(externalDependencies());
        for (Dependency dependency :
                allDependencies) {
            model.addDependency(dependency);
        }

        Properties proper = model.getProperties();
        proper.setProperty("maven.compiler.source", "1.8");
        proper.setProperty("maven.compiler.target", "1.8");

        Build build = new Build();
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-shade-plugin");
        plugin.setVersion("2.3");

        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setPhase("package");
        pluginExecution.addGoal("shade");

        plugin.addExecution(pluginExecution);

        Xpp3Dom config = new Xpp3Dom("configuration");
        Xpp3Dom createDependencyReducedPom = new Xpp3Dom("createDependencyReducedPom");
        config.addChild(createDependencyReducedPom);
        createDependencyReducedPom.setValue("false");
        plugin.setConfiguration(config);
        build.addPlugin(plugin);
        model.setBuild(build);
        model.setModelVersion("4.0.0");

        File pom = new File(path + "/pom.xml");
        try {
            Writer writer = new PrintWriter(pom);
            new MavenXpp3Writer().write(writer, model);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates default list dependencies for AWS lambda functions
     *
     * @return {@link List<Dependency>}
     */
    private List<Dependency> dependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        String[][] dependenciesAtributes =
                {{"com.amazonaws", "aws-lambda-java-core", "1.1.0"},
                        {"com.amazonaws", "aws-java-sdk-lambda", "1.11.60"},
                        {"org.aspectj", "aspectjrt", "1.8.2"},
                        {"com.googlecode.json-simple", "json-simple", "1.1"},
                        {"com.fasterxml.jackson.core", "jackson-annotations", "2.8.5"}};
        for (int i = 0; i < dependenciesAtributes.length; i++) {
            Dependency dependency = new Dependency();
            dependency.setGroupId(dependenciesAtributes[i][0]);
            dependency.setArtifactId(dependenciesAtributes[i][1]);
            dependency.setVersion(dependenciesAtributes[i][2]);
            dependencies.add(dependency);
        }
        return dependencies;
    }

    /**
     * Creates dependencies for external jars in the classpath
     *
     * @return {@link List<Dependency>} of generated dependencies
     */
    private List<Dependency> externalDependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        for (URL url :
                externalLibs) {
            String systemPath = url.getPath();
            if (!systemPath.endsWith(".jar")) {
                continue;
            }
            Dependency dependency = new Dependency();
            dependency.setGroupId("external");
            int artifactIdPosition = systemPath.split("/").length - 1;
            dependency.setArtifactId(String.valueOf(systemPath.split("/")[artifactIdPosition]).split(".jar")[0]);
            dependency.setVersion("Stable");
            dependency.setScope("system");
            dependency.setSystemPath(systemPath);
            dependency.setOptional(true);
            dependencies.add(dependency);
        }
        return dependencies;
    }
}
