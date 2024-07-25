public class CommandDetails {
    public final CmdType cmdType;
    public final String arg1;
    public final Integer arg2;

    public CommandDetails(CmdType cmdType, String arg1, Integer arg2) {
        this.cmdType = cmdType;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
}