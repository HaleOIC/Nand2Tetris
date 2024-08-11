import java.util.HashMap;

public class SymbolTable {
    // two kind of symbols: variables and labels
    private HashMap<String, Variable> symbolTable;
    private int thisIndex, staticIndex, argumentIndex, localIndex;

    // constructor
    public SymbolTable() {
        this.symbolTable = new HashMap<>();
    }

    public void clear() {
        this.symbolTable.clear();
        this.thisIndex = 0;
        this.staticIndex = 0;
        this.argumentIndex = 0;
        this.localIndex = 0;
    }

    public void define(String name, String type, String kind) {
        Variable var = new Variable(name, type, kind);
        switch (kind) {
            case "static":
                var.setNumber(staticIndex++);
                break;
            case "field":
                var.setNumber(thisIndex++);
                break;
            
            case "var":
                var.setNumber(localIndex++);
                break;
            default: 
                var.setNumber(argumentIndex++);
                break;
        }
        this.symbolTable.put(name, var);
    }

    /// return the number of variables of a certain kind
    public int varCount(String kind) {
        switch (kind) {
            case "static":
                return staticIndex;
            case "field":
                return thisIndex;
            case "argument":
                return argumentIndex;
            case "var":
                return localIndex;
            default:
                break;
        }
        return 0;
    }

    /// return the kind of a variable
    public String kindOf(String name) {
        return this.symbolTable.get(name).getKind();
    }


    /// return the type of a variable
    public String typeOf(String name) {
        return this.symbolTable.get(name).getType();
    }

    /// return the index of a variable
    public int indexOf(String name) {
        return this.symbolTable.get(name).getNumber();
    }

    /// return whether a variable is in the table
    public boolean contains(String name) {
        return this.symbolTable.containsKey(name);
    }
}
