package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;
import java.util.List;

/**
 * PoJo class entity for input type
 */
public class InputTypeEntity extends PoJoEntity {

    /** This "weird" name is used to avoid any unintentional clashes with real variables */
    public static final String EXPLICIT_THIS_FIELD = "termiteExplicitThis";

    public InputTypeEntity(String className, String qualifiedParentType, MethodTree method, List<? extends TypeMirror> inputTypes) {
        super(className);
        if (!method.getModifiers().getFlags().contains(Modifier.STATIC)) {
            // Add an explicit this reference for non static methods
            importStatments.add(qualifiedParentType);
            String[] split = qualifiedParentType.split("\\.");
            String parentSimpleName = split[split.length - 1];
            fields.add(new AbstractMap.SimpleEntry<>(parentSimpleName, EXPLICIT_THIS_FIELD));
        }
        for (VariableTree var :
                method.getParameters()) {
            fields.add(new AbstractMap.SimpleEntry<>(var.getType().toString(), var.getName().toString()));
        }
        for (TypeMirror inputType : inputTypes) {
            String typeString = inputType.toString();
            if (typeString.contains(".")) {
                if (typeString.contains("<")) {
                    importStatments.addAll(resolveGenericsFromImport(typeString));
                } else {
                    importStatments.add(typeString);
                }
            }
        }
    }
}
