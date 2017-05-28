package ch.zhaw.splab.podilizerproc.awslambda;

import com.sun.source.tree.VariableTree;

import java.util.List;

public class Utility {
    /**
     * Replace the first letter of input string to the same uppercase letter
     *
     * @param string is input String to translation
     * @return input string with first letter to upper case
     */
    public static String firstLetterToUpperCase(String string) {
        String first = string.substring(0, 1);
        String second = string.substring(1, string.length());
        first = first.toUpperCase();
        return first + second;
    }

    /**
     * Selects fields that need to be added to lambda function class
     *
     * @return fields as {@link String} of of java code to be included;
     */
    public static String fieldsToString(List<VariableTree> fields) {
        StringBuilder result = new StringBuilder();
        for (VariableTree field :
                fields) {
            result.append("\t" + field.toString() + ";\n");
        }
        return String.valueOf(result);
    }
}
