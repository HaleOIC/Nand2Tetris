import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JackTokenizer {
    ArrayList<String> tokens = new ArrayList<String>();
    int currentIndex = 0;

    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void", "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    ));

    /// Constructor
    public JackTokenizer(String inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        String total_line = "";

        while ((line = reader.readLine()) != null) {
            // filter out comments and empty lines
            line = line.trim();

            // remove inline comments
            line = line.indexOf("//") == -1 ? line : line.substring(0, line.indexOf("//"));
            if (line.isEmpty()) {
                continue;
            }
            total_line += line;
        }
        reader.close();

        // remove block comments
        String uncommentedCode = total_line.replaceAll("/\\*\\*.*?\\*/", "");

        // Manually split into tokens
        boolean inString = false;
        StringBuilder currentToken = new StringBuilder();
        for (char c : uncommentedCode.toCharArray()) {
            if (c == '"' && !inString) {
                inString = true;
                currentToken.append(c);
            } else if (c == '"' && inString) {
                currentToken.append(c);
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
                inString = false;
            } else if (inString) {
                currentToken.append(c);
            } else if (!inString && "{}()[].,;+-*/|<>=~".indexOf(c) != -1) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
                tokens.add(Character.toString(c));
            } else if (!inString && Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString().trim());
        }
    }
    
    /// Are there more tokens in the input?
    public boolean hasMoreTokens() {
        return currentIndex < tokens.size();
    }

    /// Gets the next token from the input and makes it the current token.
    public void advance() {
        if (hasMoreTokens()) {
            currentIndex++;
        }
    }

    /// Returns the type of the current token.
    public String tokenType() {
        String token = tokens.get(currentIndex);
        if (token.matches("\\d+")) {
            int value = Integer.parseInt(token);
            if (value >= 0 && value <= 32767) {
                return "integerConstant";
            }
        }
        if (token.matches("^\"[^\"]*\"$")) {
            return "stringConstant";
        }
        if ("{}()[].,;+-*/&|<>=~".contains(token)) {
            return "symbol";
        }
        if (KEYWORDS.contains(token)) {
            return "keyword";
        }
        if (token.matches("^[a-zA-Z_]\\w*$")) {
            return "identifier";
        }
        
        return "unknown";
    }

    /// Returns the keyword which is the current token.
    public String keyWord() {
        String currentType = tokenType();
        if (currentType == "keyword") {
            return stringVal();
        } else {
            throw new IllegalArgumentException("Current token is not a keyword: " + stringVal());
        }
    }

    /// Returns the identifier which is the current token.
    public String identifier() {
        String currentType = tokenType();
        if (currentType == "identifier") {
            return stringVal();
        } else {
            throw new IllegalArgumentException("Current token is not a identifier: " + stringVal());
        }
    }

    /// Returns the symbol which is the current token 
    public String symbol() {
        String currentType = tokenType();
        if (currentType == "symbol") {
            return stringVal();
        } else {
            throw new IllegalArgumentException("Current token is not a symbol: " + stringVal());
        }
    }

    /// Return 
    public String type() {
        String currentType = tokenType();
        if (currentType == "identifier" || "int char boolean".contains(stringVal())) {
            return stringVal();
        } else {
            throw new IllegalArgumentException("Current token is not a type: " + stringVal());
        }
    }

    public String routineType() {
        String currentType = tokenType();
        if (currentType == "identifier" || "int char boolean void".contains(stringVal())) {
            return stringVal();
        } else {
            throw new IllegalArgumentException("Current token is not a type: " + stringVal());
        }
    }


    /// Returns the string value of the current token, without the double quotes.
    public String stringVal() {
        String token = tokens.get(currentIndex);
        return token;
    }
}
