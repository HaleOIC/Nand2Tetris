public class Variable {
    /// Two levels of scope
    static final int CLASSLEVEL = 0;
    static final int SUBROUTINELEVEL = 1;
    
    /// each variable has its own kind, type, name and scope
    private String kind;
    private String type;
    private String name;
    private int scope;
    private int number;

    public Variable(String name, String type, String kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }
    public String getKind() {
        return kind;
    }
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public int getScope() {
        return scope;
    }
    public void setNumber(int id) { 
        this.number = id;
    }    
    public int getNumber() {
        return number;
    }
}
