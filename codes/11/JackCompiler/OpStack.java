import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class OpStack {

    private Stack<String> stack;
    private Map<String, Integer> precedence;
    private VMWriter writer;

    public OpStack(VMWriter writer) {
        this.stack = new Stack<>();
        this.precedence = new HashMap<>();
        this.writer = writer;
        
        // Define precedence for operators
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);
        precedence.put("&", 3);
        precedence.put("|", 3);
        precedence.put("<", 0);
        precedence.put(">", 0);
        precedence.put("=", 0);
        precedence.put("neg", 4); // Unary minus with higher precedence
        precedence.put("~", 4); // Bitwise NOT with higher precedenceßß
        precedence.put("(", 0);
    }

    public void push(String op) {
        // when push a new operator, pop the previous operator and write the operation
        if (op.equals("(") || op.equals("[")) {
            stack.push(op);
            return;
        }
        while (!stack.isEmpty() && getPrecedence(op) < getPrecedence(stack.peek())) {
            if (stack.peek().equals("[") || stack.peek().equals("(")) {
                break;
            }
            String prevOp = stack.pop();
            if (prevOp.equals("neg")) {
                writer.writeArithmetic("neg");
            } else if (prevOp.equals("~")) {
                writer.writeArithmetic("not");
            } else {
                writeOperation(prevOp);
            }
        }
        stack.push(op);
    }

    public String pop() {
        return stack.pop();
    }

    public String peek() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int getPrecedence(String op) {
        return precedence.getOrDefault(op, -1);
    }

    public void popUntil() {
        while (!stack.isEmpty() && !stack.peek().equals("(")) {
            String prevOp = stack.pop();
            if (prevOp.equals("neg")) {
                writer.writeArithmetic("neg");
            } else if (prevOp.equals("~")) {
                writer.writeArithmetic("not");
            } else {
                writeOperation(prevOp);
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        stack.pop(); // Pop the left parenthesis
    }

    public void popUntilLastBracket() {
        while (!stack.isEmpty() && !stack.peek().equals("[")) {
            String prevOp = stack.pop();
            if (prevOp.equals("neg")) {
                writer.writeArithmetic("neg");
            } else if (prevOp.equals("~")) {
                writer.writeArithmetic("not");
            } else {
                writeOperation(prevOp);
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        stack.pop();
    }

    public void clear() {
        stack.clear();
    }

    private void writeOperation(String op) {
        // write the operation
        if (op.equals("+")) {
            writer.writeArithmetic("add");
        } else if (op.equals("-")) {
            writer.writeArithmetic("sub");
        } else if (op.equals("*")) {
            writer.writeCall("Math.multiply", 2);
        } else if (op.equals("/")) {
            writer.writeCall("Math.divide", 2);
        } else if (op.equals("&")) {
            writer.writeArithmetic("and");
        } else if (op.equals("|")) {
            writer.writeArithmetic("or");
        } else if (op.equals("<")) {
            writer.writeArithmetic("lt");
        } else if (op.equals(">")) {
            writer.writeArithmetic("gt");
        } else if (op.equals("=")) {
            writer.writeArithmetic("eq");
        }
    }

    public boolean contains(String op) {
        return stack.contains(op);
    }
}