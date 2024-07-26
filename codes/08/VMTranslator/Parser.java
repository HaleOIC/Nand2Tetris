import java.util.HashMap;
import java.util.Map;

public class Parser {
    private static final Map<String, CmdType> COMMAND_TABLE = new HashMap<>();
    static {
        COMMAND_TABLE.put("push", CmdType.CPush);
        COMMAND_TABLE.put("pop", CmdType.CPop);
        COMMAND_TABLE.put("add", CmdType.CArithmetic);
        COMMAND_TABLE.put("sub", CmdType.CArithmetic);
        COMMAND_TABLE.put("neg", CmdType.CArithmetic);
        COMMAND_TABLE.put("eq", CmdType.CArithmetic);
        COMMAND_TABLE.put("lt", CmdType.CArithmetic);
        COMMAND_TABLE.put("and", CmdType.CArithmetic);
        COMMAND_TABLE.put("or", CmdType.CArithmetic);
        COMMAND_TABLE.put("not", CmdType.CArithmetic);
        COMMAND_TABLE.put("label", CmdType.CLabel);
        COMMAND_TABLE.put("goto", CmdType.CGoto);
        COMMAND_TABLE.put("function", CmdType.CFunction);
        COMMAND_TABLE.put("if-goto", CmdType.CIf);
        COMMAND_TABLE.put("call", CmdType.CCall);
        COMMAND_TABLE.put("return", CmdType.CReturn);
    }

    public static CommandDetails parseCommand(String command) {
        String[] cmdElements = command.split("\\s+");

        CmdType cmdType = COMMAND_TABLE.getOrDefault(cmdElements[0], CmdType.CArithmetic);

        String arg1 = null;
        Integer arg2 = null;

        if (cmdType == CmdType.CArithmetic) {
            arg1 = cmdElements[0];
        } else if (cmdElements.length > 1) {
            arg1 = cmdElements[1];
        }

        if (cmdElements.length == 3) {
            try {
                arg2 = Integer.parseInt(cmdElements[2]);
            } catch (NumberFormatException e) {
                arg2 = null;
            }
        }

        return new CommandDetails(cmdType, arg1, arg2);
    }
}