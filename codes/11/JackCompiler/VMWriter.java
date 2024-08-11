import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter writer;
    
    VMWriter(String fileName) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(fileName));
    }

    public void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /// segment can be CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP
    public void writePush(String segment, int index) {
        try {
            this.writer.write("push " + segment + " " + index + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePop(String segment, int index) {
        try{
            this.writer.write("pop " + segment + " " + index + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        try {
            this.writer.write(command + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeLabel(String label) {
        try {
            this.writer.write("label " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeGoto(String label) {
        try {
            this.writer.write("goto " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIf(String label) {
        try {
            this.writer.write("if-goto " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCall(String name, int nArgs) {
        try {
            this.writer.write("call " + name + " " + nArgs + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFunction(String name, int nLocals) {
        try {
            this.writer.write("function " + name + " " + nLocals + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeReturn() {
        try {
            this.writer.write("return\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePlainText(String text) {
        try {
            this.writer.write(text + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
