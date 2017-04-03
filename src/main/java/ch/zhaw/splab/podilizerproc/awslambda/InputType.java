package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.VariableTree;

import java.util.List;


public class InputType {
    private List<VariableTree> fields;

    public InputType(List<VariableTree> fields, List<? extends VariableTree> parameters) {
        this.fields = fields;
        fields.addAll(parameters);
    }
    public String create(){
        String result = "public class InputType{\n";
        result += Utility.fieldsToString(fields) + "\n";
        result += generateConstructor();
        result += generateGetters();
        result += generateSetters();
        result += "}";
        return result;
    }

    /**
     * Generates constructor block which initialize all fields
     * @return java code of constructor as a {@link String}
     */
    private String generateConstructor(){
        String result = "\tpublic InputType(";
        String constructorBody = "";
        int i = 0;
        for (VariableTree field :
                fields) {
            String var = field.getType() + " " + field.getName();
            if (i == 0){
                result += var;
            } else {
                result += ", " + var;
            }
            i++;
            constructorBody += "\t\tthis." + field.getName() + " = " + field.getName() + ";\n";
        }
        result += "){\n" + constructorBody + "\t}\n";
        return result;
    }

    /**
     * Generates getter for each field
     * @return getters as a {@link String} of formatted java code
     */
    private String generateGetters(){
        String result = "";
        for (VariableTree field :
                fields) {
            result += "\tpublic " + field.getType() + " get" +
                    Utility.firstLetterToUpperCase(field.getName().toString() + "(){\n" +
                            "\t\treturn " + field.getName() + ";\n\t}\n");

        }
        return result;
    }

    /**
     * Generates setter for each field
     * @return setters as a {@link String} of formatted java code
     */
    private String generateSetters(){
        String result = "";
        for (VariableTree field :
                fields) {
            result += "\tpublic void set" + Utility.firstLetterToUpperCase(field.getName().toString() +
                    "(" + field.getType() + " " + field.getName() + "){\n" +
                    "\t\tthis." + field.getName() + " = " + field.getName() + ";\n\t}\n");
        }
        return result;
    }
}
