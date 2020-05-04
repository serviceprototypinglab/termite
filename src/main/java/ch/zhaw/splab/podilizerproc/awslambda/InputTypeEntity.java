package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.VariableTree;

import java.util.AbstractMap;
import java.util.List;

/**
 * PoJo class entity for input type
 */
public class InputTypeEntity extends PoJoEntity {

    public InputTypeEntity(String className, List<? extends VariableTree> params) {
        super(className);
        for (VariableTree var :
                params) {
            fields.add(new AbstractMap.SimpleEntry<String, String>(var.getType().toString(), var.getName().toString()));
        }
    }
}
