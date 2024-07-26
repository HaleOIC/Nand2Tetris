import java.io.*;

public class VMTranslator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java VMTranslator <vm_file>");
            System.exit(1);
        }

        String file = args[0];

        // extension is .vm
        if (file.endsWith(".vm")) {
            String outputFilePath = file.substring(0, file.length() - 3) + ".asm";
            try {
                translateFile(file, outputFilePath);
            } catch (IOException e) {
                System.err.println("Error processing file '" + file + "': " + e.getMessage());
                System.exit(1);
            }
        } else {
            // is a directory
            int lastSlashIndex = file.lastIndexOf('/');
            String outputFilePath;
            if (lastSlashIndex != -1) {
                outputFilePath = file + "/" + file.substring(lastSlashIndex + 1) + ".asm";
            } else {
                outputFilePath = file + "/" + file + ".asm";
            }
            try {
                translateDirectory(file, outputFilePath);
            } catch (IOException e) {
                System.err.println("Error processing directory '" + file + "': " + e.getMessage());
                System.exit(1);
            }
        }
    }
    
    private static void translateFile(String inputFilePath, String outputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        CodeWriter codeWriter = new CodeWriter(outputFilePath);

        // parse the file
        codeWriter.setFileName(inputFile.getName());
        parseFile(reader, codeWriter);


        // close the reader and writer
        reader.close();
        codeWriter.close();
    }

    private static void translateDirectory(String inputDirectory, String outputFilePath) throws IOException {
        File directory = new File(inputDirectory);
        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("Error: The directory '" + inputDirectory + "' is empty");
            System.exit(1);
        }
    
        // Initialize CodeWriter
        CodeWriter codeWriter = new CodeWriter(outputFilePath);
        codeWriter.writeInit();
    
        // Parse each VM file except Sys.vm
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".vm")) {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                codeWriter.setFileName(f.getName());
                parseFile(reader, codeWriter);
            }
        }
        codeWriter.close();
    }
    

    private static void parseFile(BufferedReader reader, CodeWriter codeWriter) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            // remove leading and trailing whitespaces
            line = line.trim();
            int index = line.indexOf("//");
            if (index != -1) {
                line = line.substring(0, index);
            }
            if (line.isEmpty()) {
                continue;
            }
            // parse the command
            CommandDetails commandDetails = Parser.parseCommand(line);
            codeWriter.writeAssembly(commandDetails.cmdType, commandDetails.arg1, commandDetails.arg2);
        }
    }
}
