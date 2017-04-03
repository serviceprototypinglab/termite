package ch.zhaw.splab.podilizerproc.awslambda;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.*;
import java.nio.file.Path;

public class PomGenerator {
    Path path;

    public PomGenerator(Path path) {
        this.path = path;
    }

    public void create(){
        Model model = new Model();
        model.setGroupId("some.group.id");
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
}
