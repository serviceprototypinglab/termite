package ch.zhaw.splab.podilizerproc.awslambda;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class PoJoEntity {
    private String className;
    protected List<Pair<String, String>> fields;

    public PoJoEntity(String className) {
        this.className = className;
        fields = new ArrayList<>();
    }

    public String create() {
        String result = "public class " + className + "{\n";
        result += generateFieldsDeclaration() + "\n";
        result += generateDefaultConstructor();
        result += generateConstructor();
        result += generateGetters();
        result += generateSetters();
        result += "}";
        return result;
    }

    /**
     * Generates default constructor
     *
     * @return formatted {@link String} of default constructor declaration
     */
    private String generateDefaultConstructor() {
        return "\tpublic " + className + "(){\n" +
                "\t};\n";
    }

    /**
     * Generates constructor block which initialize all fields
     *
     * @return java code of constructor as a {@link String}
     */
    private String generateConstructor() {
        String result = "\tpublic " + className + "(";
        String constructorBody = "";
        int i = 0;
        for (Pair<String, String> entry : fields) {
            String var = entry.getKey() + " " + entry.getValue();
            if (i == 0) {
                result += var;
            } else {
                result += ", " + var;
            }
            i++;
            constructorBody += "\t\tthis." + entry.getValue() + " = " + entry.getValue() + ";\n";
        }
        result += "){\n" + constructorBody + "\t}\n";
        return result;
    }

    /**
     * Generates getter for each field
     *
     * @return getters as a {@link String} of formatted java code
     */
    private String generateGetters() {
        String result = "";
        for (Pair<String, String> entry : fields) {
            result += "\tpublic " + entry.getKey() + " get" +
                    Utility.firstLetterToUpperCase(entry.getValue() + "(){\n" +
                            "\t\treturn " + entry.getValue() + ";\n\t}\n");
        }
        return result;
    }

    /**
     * Generates setter for each field
     *
     * @return setters as a {@link String} of formatted java code
     */
    private String generateSetters() {
        String result = "";
        for (Pair<String, String> entry : fields) {
            result += "\tpublic void set" + Utility.firstLetterToUpperCase(entry.getValue() +
                    "(" + entry.getKey() + " " + entry.getValue() + "){\n" +
                    "\t\tthis." + entry.getValue() + " = " + entry.getValue() + ";\n\t}\n");
        }
        return result;
    }

    /**
     * Generates fields declaration for PoJo
     *
     * @return formatted java code of field declarations
     */
    private String generateFieldsDeclaration() {
        String result = "";
        for (Pair<String, String> entry : fields) {
            result += "\tprivate " + entry.getKey() + " " + entry.getValue() + ";\n";
        }
        return result;
    }
}
