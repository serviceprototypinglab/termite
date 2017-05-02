package ch.zhaw.splab.podilizerproc.awslambda;


import com.sun.source.tree.Tree;

public class OutputType {
    private String defaultReturn;
    private Tree returnType;
    private boolean isVoid;

    public OutputType(Tree returnType) {
        this.returnType = returnType;
        if (returnType.toString().equals("void")){
            isVoid = true;
        } else {
            isVoid = false;
        }
    }

    public String create(){
        String result = "public class OutputType{\n" +
                "\tprivate String defaultReturn;\n";
        if (!isVoid){
            result += "\tprivate " + returnType.toString() + " result;\n";
        }
        result += generateDefaultConstructor();
        result += generateConstructor();
        result += generateGetters();
        result += generateSetters();
        result += "};";
        return result;
    }
    /**
     * Generates default constructor for OutputType class
     * @return formatted {@link String} of default constructor declaration
     */
    private String generateDefaultConstructor(){
        return "\tpublic OutputType(){\n" +
                "\t};\n\n";
    }

    /**
     * Generates constructor for OutputType class
     * @return formatted {@link String} of constructor declaration
     */
    private String generateConstructor(){
        String result = "\tpublic OutputType(String defaultReturn";
        if (!isVoid){
            result += ", " + returnType.toString() + " result";
        }
        result += "){\n" +
                "\t\tthis.defaultReturn = defaultReturn;\n";
        if (!isVoid){
            result += "\t\tthis.result = result;\n";
        }
        result += "\t};\n\n";
        return result;
    }

    /**
     * Generates getters for every field in OutputType class
     * @return formatted {@link String}
     */
    private String generateGetters(){
        String result = "\tpublic String getDefaultReturn(){\n" +
                "\t\treturn defaultReturn;\n" +
                "\t};\n";
        if (!isVoid){
            result += "\tpublic " + returnType.toString() + " getResult(){\n" +
                    "\t\treturn result;\n" +
                    "\t};\n";
        }
        return result;
    }
    /**
     * Generates setters for every field in OutputType class
     * @return formatted {@link String}
     */
    private String generateSetters(){
        String result = "\tpublic void setDefaultReturn(String defaultReturn){\n" +
                "\t\tthis.defaultReturn = defaultReturn;\n" +
                "\t};\n";
        if (!isVoid){
            result += "\tpublic void setResult(" + returnType.toString() + " result);\n" +
                    "\t\tthis.result = result;\n" +
                    "\t};\n";
        }
        return result;
    }
}
