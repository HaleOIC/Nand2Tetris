import java.io.IOException;

public class JackCompiler {
    public static void main(String[] args) throws IOException {
        String file = args[0];

        // Case1: single file
        if (file.endsWith(".jack")) {
            String outputFile = file.substring(0, file.lastIndexOf('.')) + ".vm";
            parseFile(file, outputFile);
        } else {
            // Case2: directory
            String[] files = new java.io.File(file).list();
            for (String f : files) {
                if (f.endsWith(".jack")) {
                    parseFile(file + "/" + f, file + "/" + f.substring(0, f.lastIndexOf('.')) + ".vm");
                }
            }
        }
    }

    /// Parse the input file and write the output to the output file
    public static void parseFile(String inputFile, String outputFile) throws IOException {
        JackTokenizer jt = new JackTokenizer(inputFile);
        CompilationEngine ce = new CompilationEngine(jt, outputFile);
        ce.compileClass();
    }
}