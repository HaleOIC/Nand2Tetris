import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private int jumpCounter = 0;
    private BufferedWriter outPrinter;
    private int labelCounter = 0;
    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public CodeWriter(String writer) throws IOException {
        this.outPrinter = new BufferedWriter(new FileWriter(writer));
    }

    public void close() throws IOException {
        outPrinter.close();
    }

    public void writeAssembly(CmdType cmdType, String arg1, Integer arg2) throws IOException {
        String asmCode = "";
        switch (cmdType) {
            case CArithmetic:
                asmCode = writeArithmetic(arg1);
                break;
            case CPush:
                asmCode = writePush(arg1, arg2);
                break;
            case CPop:
                asmCode = writePop(arg1, arg2);
                break;
            case CLabel:
                asmCode = String.format("(%s)\n", arg1);
                break;
            case CGoto:
                asmCode = String.format("@%s\n0;JMP\n", arg1);
                break;
            case CIf:
                asmCode = "@SP\nAM=M-1\nD=M\nA=A+1\n@" + arg1 + "\nD;JNE\n";
                break;
            case CFunction:
                asmCode = writeFunction(arg1, arg2);
                break;
            case CCall:
                asmCode = writeCall(arg1, arg2);
                break;
            case CReturn:
                asmCode = writeReturn();
                break;
            default:
                throw new UnsupportedOperationException("Command type not supported.");
        }
        outPrinter.write(asmCode);
    }


    private String getSegment(String segment, int index) {
        switch (segment) {
            case "local":
                return "LCL";
            case "argument":
                return "ARG";
            case "this":
                return "THIS";
            case "that":
                return "THAT";
            case "pointer":
                return (index == 0) ? "THIS" : "THAT";
            default:
                throw new IllegalArgumentException("Invalid segment");
        }
    }

    private boolean getDirect(String segment) {
        return segment.equals("pointer") || segment.equals("static");
    }

    private String writeArithmetic(String command) {
        String result = "";
        switch (command) {
            case "add":
                result = arithmeticTemplate1() + "M=M+D\n";
                break;
            case "sub":
                result = arithmeticTemplate1() + "M=M-D\n";
                break;
            case "and":
                result = arithmeticTemplate1() + "M=M&D\n";
                break;
            case "or":
                result = arithmeticTemplate1() + "M=M|D\n";
                break;
            case "gt":
            case "lt":
            case "eq":
                String jumpType = command.equals("gt") ? "JLE" :
                                  command.equals("lt") ? "JGE" : "JNE";
                jumpCounter++;
                result = arithmeticTemplate2(jumpType);
                break;
            case "not":
                result = "@SP\nA=M-1\nM=!M\n";
                break;
            case "neg":
                result = "D=0\n@SP\nA=M-1\nM=D-M\n";
                break;
            default:
                throw new UnsupportedOperationException("Arithmetic command not supported.");
        }
        return result;
    }

    public void writeInit() {
        String initCode = "@256\nD=A\n@SP\nM=D\n" + writeCall("Sys.init", jumpCounter);
        try {
            outPrinter.write(initCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String writePush(String segment, int index) {
        if (segment.equals("constant")) {
            return String.format("@%d\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", index);
        } else if (segment.equals("static")) {
            return String.format("@%s.%d\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", fileName, index);
        }
        String seg;
        boolean isDirect;
        if (segment.equals("temp")) {
            seg = "R" + (5 + index);
            isDirect = true;
        } else {
            seg = getSegment(segment, index);
            isDirect = getDirect(segment);
        }
        return pushTemplate(seg, index, isDirect);
    }

    private String writePop(String segment, int index) {
        if (segment.equals("temp")) {
            return String.format("@SP\nAM=M-1\nD=M\n@R%d\nM=D\n", 5 + index);
        } else if (segment.equals("static")) {
            return String.format("@%s.%d\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n", fileName, index);
        }
        String seg;
        boolean isDirect;
        if (segment.equals("static")) {
            seg = String.valueOf(16 + index);
            isDirect = true;
        } else {
            seg = getSegment(segment, index);
            isDirect = getDirect(segment);
        }
        return popTemplate(seg, index, isDirect);
    }

    private String writeFunction(String functionName, int numLocals) {
        StringBuilder functionCode = new StringBuilder();
        functionCode.append(String.format("(%s)\n", functionName));
        for (int i = 0; i < numLocals; i++) {
            functionCode.append(writePush("constant", 0));
        }
        return functionCode.toString();
    }

    private String writeCall(String functionName, int numArgs) {
        // Generate a unique return label for the function call
    String returnLabel = "RETURN_LABEL" + (labelCounter++);
    StringBuilder sb = new StringBuilder();

    // Append return address onto the stack instructions to StringBuilder
    sb.append("@" + returnLabel + "\n" + 
              "D=A\n" +             // Place return label address in D register
              "@SP\n" +             // Set A register to SP (stack pointer)
              "A=M\n" +             // Dereference SP to get the top of the stack
              "M=D\n" +             // Write return address from D register to stack
              "@SP\n" +             // Set A register to SP again
              "M=M+1\n");           // Increment SP

    // Append LCL, ARG, THIS, and THAT as part of the call environment
    sb.append(pushTemplate("LCL", 0, true));  // push LCL
    sb.append(pushTemplate("ARG", 0, true));  // push ARG
    sb.append(pushTemplate("THIS", 0, true)); // push THIS
    sb.append(pushTemplate("THAT", 0, true)); // push THAT

    // Setup ARG = SP-n-5 for the called function
    sb.append("@SP\n" +
              "D=M\n" +             // D = current SP
              "@5\n" +              // Constant 5 for the offset
              "D=D-A\n" +           // D = SP - 5
              "@" + numArgs + "\n" + // number of arguments
              "D=D-A\n" +           // D = SP - 5 - numArgs
              "@ARG\n" +            // Set ARG for the callee
              "M=D\n");             // ARG = D

    // Setup LCL = SP for the called function
    sb.append("@SP\n" +
              "D=M\n" +             // D = current SP
              "@LCL\n" +            // address of LCL
              "M=D\n");             // LCL = SP

    // Perform the function call
    sb.append("@" + functionName + "\n" +  // Address of the function
              "0;JMP\n" +          // Unconditional jump to the function
              "(" + returnLabel + ")\n");  // Mark the return point

    return sb.toString();
    }

    private String writeReturn() {
        return "@LCL\n" +
                "D=M\n" +
                "@R11\n" +
                "M=D\n" +
                "@5\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@R12\n" +
                "M=D\n" +
                popTemplate("ARG", 0, false) +
                "@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1\n" +
                preFrameTemplate("THAT") +
                preFrameTemplate("THIS") +
                preFrameTemplate("ARG") +
                preFrameTemplate("LCL") +
                "@R12\n" +
                "A=M\n" +
                "0;JMP\n";
    }

    private String preFrameTemplate(String position){

        return "@R11\n" +
                "D=M-1\n" +
                "AM=D\n" +
                "D=M\n" +
                "@" + position + "\n" +
                "M=D\n";

    }

    private String arithmeticTemplate1() {
        return "@SP\nAM=M-1\nD=M\nA=A-1\n";
    }

    private String arithmeticTemplate2(String jumpType) {
        return String.format("@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D\n@FALSE%d\nD;%s\n@SP\nA=M-1\nM=-1\n@CONTINUE%d\n0;JMP\n(FALSE%d)\n@SP\nA=M-1\nM=0\n(CONTINUE%d)\n",
                jumpCounter, jumpType, jumpCounter, jumpCounter, jumpCounter);
    }

    private String pushTemplate(String segment, int index, boolean isDirect) {
        String indexCode = isDirect ? "" : String.format("@%d\nA=D+A\nD=M\n", index);
        return String.format("@%s\nD=M\n%s@SP\nA=M\nM=D\n@SP\nM=M+1\n", segment, indexCode);
    }

    private String popTemplate(String segment, int index, boolean isDirect) {
        String pointerAdjust = isDirect ? "D=A\n" : String.format("D=M\n@%d\nD=D+A\n", index);
        return String.format("@%s\n%s@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n", segment, pointerAdjust);
    }
}
