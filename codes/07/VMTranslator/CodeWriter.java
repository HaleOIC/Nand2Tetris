import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private int jumpCounter = 0;
    private BufferedWriter outPrinter;

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
            case "static":
                return Integer.toString(16 + index);
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

    private String writePush(String segment, int index) {
        if (segment.equals("constant")) {
            return String.format("@%d\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", index);
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
