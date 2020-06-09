package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.Tree;

import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;

/**
 * PoJo class entity for output type
 */
public class OutputTypeEntity extends PoJoEntity {

    public OutputTypeEntity(String className, Tree returnType, TypeMirror resultType) {
        super(className);
        fields.add(new AbstractMap.SimpleEntry<>("String", "defaultReturn"));
        fields.add(new AbstractMap.SimpleEntry<>("long", "time"));
        if (!returnType.toString().equals("void")) {
            fields.add(new AbstractMap.SimpleEntry<>(returnType.toString(), "result"));
        }
        String resultTypeStr = resultType.toString();
        if (resultTypeStr.contains(".")) {
            importStatments.add(resultTypeStr);
        }
    }
}
