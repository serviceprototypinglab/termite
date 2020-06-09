package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.VariableTree;

import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;
import java.util.List;

/**
 * PoJo class entity for input type
 */
public class InputTypeEntity extends PoJoEntity {

    public InputTypeEntity(String className, List<? extends VariableTree> params, List<? extends TypeMirror> inputTypes) {
        super(className);
        for (VariableTree var :
                params) {
            fields.add(new AbstractMap.SimpleEntry<String, String>(var.getType().toString(), var.getName().toString()));
        }
        for (TypeMirror inputType : inputTypes) {
            String typeString = inputType.toString();
            if (typeString.contains(".")) {
                System.out.println("INPUT IMPORT: " + typeString);
                importStatments.add(typeString);
            }
        }


    }
}
