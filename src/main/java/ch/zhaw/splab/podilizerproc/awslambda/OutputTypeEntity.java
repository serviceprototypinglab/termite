package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.Tree;
import javafx.util.Pair;

/**
 * PoJo class entity for output type
 */
public class OutputTypeEntity extends PoJoEntity {

    public OutputTypeEntity(String className, Tree returnType) {
        super(className);
        fields.add(new Pair<>("String", "defaultReturn"));
        fields.add(new Pair<>("long", "time"));
        if (!returnType.toString().equals("void")){
            fields.add(new Pair<>(returnType.toString(), "result"));
        }
    }
}
