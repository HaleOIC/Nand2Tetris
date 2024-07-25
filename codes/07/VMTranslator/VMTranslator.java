import java.io.*;

public class VMTranslator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java VMTranslator <vm_file>");
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String file = args[i];
            if (file.endsWith(".vm")) {
                String outputFilePath = file.substring(0, file.length() - 3) + ".asm";

                try {
                    translateFile(file, outputFilePath);
                } catch (IOException e) {
                    System.err.println("Error processing file '" + file + "': " + e.getMessage());
                    System.exit(1);
                }
            } else {
                System.err.println("Error: The file '" + file + "' does not have a .vm extension");
                System.exit(1);
            }
        }
    }

    private static void translateFile(String inputFilePath, String outputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        CodeWriter codeWriter = new CodeWriter(outputFilePath);
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            CommandDetails commandDetails = Parser.parseCommand(line);
            codeWriter.writeAssembly(commandDetails.cmdType, commandDetails.arg1, commandDetails.arg2);
        }
        reader.close();
        codeWriter.close();
    }
}
