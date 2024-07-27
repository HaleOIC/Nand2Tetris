import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer jt;
    private BufferedWriter writer;
    /// Constructor
    public CompilationEngine(JackTokenizer jt, String outputFile) throws IOException {
        this.jt = jt;
        this.writer = new BufferedWriter(new FileWriter(outputFile));
    }

    /// Compiles a complete class.
    public void compileClass() throws IOException {
        try {
            // class
            String classKeyword = jt.keyWord();
            if (!classKeyword.equals("class")) {
                throw new IllegalArgumentException("Current token is not class");
            }
            writer.write("<class>\n");
            jt.advance();

            writer.write(String.format("<keyword>%s</keyword>\n", classKeyword));

            // className 
            String className = jt.identifier();
            writer.write(String.format("<identifier>%s</identifier>\n", className));
            jt.advance();
            
            // { symbol begin 
            String symbolBegin = jt.symbol();
            if (!symbolBegin.equals("{")) {
                throw new IllegalArgumentException("Current token is not {");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbolBegin));
            jt.advance();

            // compile classVarDec 
            while (compileClassVarDec());

            // compile subroutineDec
            while (compileSubroutine());

            // } symbol end
            String symbolEnd = jt.symbol();
            if (!symbolEnd.equals("}")) {
                throw new IllegalArgumentException("Current token is not }");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbolEnd));
            jt.advance();

            // end class 
            writer.write("</class>\n");
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
        writer.close();
    }

    /// Compiles a static declaration or a field declaration.
    private boolean compileClassVarDec() throws IOException {
        // static | field keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("static") && !keyword.equals("field")) {
            return false;
        }
        jt.advance();

        try {
            // write label into
            writer.write("<classVarDec>\n");
            writer.write(String.format("<keyword>%s</keyword>\n", keyword));

            // type
            String type = jt.type();
            if (JackTokenizer.KEYWORDS.contains(type)) {
                writer.write(String.format("<keyword>%s</keyword>\n", type));
            } else {
                writer.write(String.format("<identifier>%s</identifier>\n", type));
            }
            jt.advance();
            // varName
            String varName = jt.identifier();
            writer.write(String.format("<identifier>%s</identifier>\n", varName));
            jt.advance();

            // maybe more varName
            while (true) {
                try {
                    String comma = jt.symbol();
                    if (comma.equals(";")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", comma));
                        jt.advance();
                        break;
                    }
                    jt.advance();
                    String varNameMore = jt.identifier();
                    jt.advance();

                    // write into file
                    writer.write(String.format("<symbol>%s</symbol>\n", comma));
                    writer.write(String.format("<identifier>%s</identifier>\n", varNameMore));

                } catch (IllegalArgumentException e) {
                    break;
                }
            }
            writer.write("</classVarDec>\n");
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /// Compiles a complete method, function, or constructor.
    private boolean compileSubroutine() throws IOException {
        // constructor | function | method
        String keyword;
        try {
            keyword = jt.keyWord();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (!keyword.equals("constructor") && !keyword.equals("function") && !keyword.equals("method")) {
            return false;
        }
        jt.advance();

        try {
            // write label into
            writer.write("<subroutineDec>\n");
            writer.write(String.format("<keyword>%s</keyword>\n", keyword));

            // return type
            String returnType = jt.routineType();
            if (JackTokenizer.KEYWORDS.contains(returnType)) {
                writer.write(String.format("<keyword>%s</keyword>\n", returnType));
            } else {
                writer.write(String.format("<identifier>%s</identifier>\n", returnType));
            }
            jt.advance();

            // subroutineName
            String subroutineName = jt.identifier();
            writer.write(String.format("<identifier>%s</identifier>\n", subroutineName));
            jt.advance();

            // (
            String symbolBegin = jt.symbol();
            if (!symbolBegin.equals("(")) {
                throw new IllegalArgumentException("Current token is not (");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbolBegin));
            jt.advance();

            // compile parameterList
            compileParameterList();

            // )
            String symbolEnd = jt.symbol();
            if (!symbolEnd.equals(")")) {
                throw new IllegalArgumentException("Current token is not )");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbolEnd));
            jt.advance();

            // compile subroutineBody
            compileSubroutineBody();

            // end subroutineDec
            writer.write("</subroutineDec>\n");
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private void compileSubroutineBody() throws IOException {
        writer.write("<subroutineBody>\n");

        try {
            String symbol = jt.symbol();
            if (!symbol.equals("{")) {
                throw new IllegalArgumentException("Current token is not {");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance();

            // compile varDec
            while (compileVarDec());

            // compile statements
            compileStatements();

            // }
            symbol = jt.symbol();
            if (!symbol.equals("}")) {
                throw new IllegalArgumentException("Current token is not }");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance(); 
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }

        writer.write("</subroutineBody>\n");
    }

    /// Compiles a (possibly empty) parameter list, not including the enclosing "()".
    private void compileParameterList() throws IOException {
        writer.write("<parameterList>\n");
        try {
            String type = jt.type();
            if (JackTokenizer.KEYWORDS.contains(type)) {
                writer.write(String.format("<keyword>%s</keyword>\n", type));
            } else {
                writer.write(String.format("<identifier>%s</identifier>\n", type));
            }
            jt.advance();

            String varName = jt.identifier();
            writer.write(String.format("<identifier>%s</identifier>\n", varName));
            jt.advance();
            while (true) {
                try {
                    String comma = jt.symbol();
                    if (comma.equals(",")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", comma));
                        jt.advance();
                    } else {
                        break;
                    }

                    type = jt.type();
                    writer.write(String.format("<keyword>%s</keyword>\n", type));
                    jt.advance();

                    varName = jt.identifier();
                    writer.write(String.format("<identifier>%s</identifier>\n", varName));
                    jt.advance();
                } catch (IllegalArgumentException e) {
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
        }
        writer.write("</parameterList>\n");
    }

    /// Compiles a var declaration.
    private boolean compileVarDec() throws IOException {
        // var keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("var")) {
            return false;
        }
        writer.write("<varDec>\n");
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // type
        String type = jt.type();
        if (JackTokenizer.KEYWORDS.contains(type)) {
            writer.write(String.format("<keyword>%s</keyword>\n", type));
        } else {
            writer.write(String.format("<identifier>%s</identifier>\n", type));
        }
        jt.advance();

        // varName
        String varName = jt.identifier();
        writer.write(String.format("<identifier>%s</identifier>\n", varName));
        jt.advance();

        // maybe more varName
        while (true) {
            try {
                String comma = jt.symbol();
                if (comma.equals(";")) {
                    writer.write(String.format("<symbol>%s</symbol>\n", comma));
                    jt.advance();
                    break;
                }
                jt.advance();
                String varNameMore = jt.identifier();
                jt.advance();

                // write into file
                writer.write(String.format("<symbol>%s</symbol>\n", comma));
                writer.write(String.format("<identifier>%s</identifier>\n", varNameMore));

            } catch (IllegalArgumentException e) {
                break;
            }
        }
        writer.write("</varDec>\n");
        return true;
        
    }

    /// Compiles a sequence of statements, not including the enclosing "{}".
    private void compileStatements() throws IOException {
        writer.write("<statements>\n");
        while (true) {
            try {
                String keyword = jt.keyWord();
                if (!"let if while do return".contains(keyword)) {
                    break;
                }
                if (keyword.equals("let")) {
                    compileLet();
                } else if (keyword.equals("if")) {
                    compileIf();
                } else if (keyword.equals("while")) {
                    compileWhile();
                } else if (keyword.equals("do")) {
                    compileDo();
                } else if (keyword.equals("return")) {
                    compileReturn();
                }
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        writer.write("</statements>\n");
    }

    /// method that compiles a let statement
    private void compileLet() throws IOException {
        writer.write("<letStatement>\n");

        // let keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("let")) {
            return;
        }
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // varName
        String varName = jt.identifier();
        writer.write(String.format("<identifier>%s</identifier>\n", varName));
        jt.advance();

        // [ expression ]
        try {
            String symbol = jt.symbol();
            if (symbol.equals("[")) {
                writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                jt.advance();

                compileExpression();

                symbol = jt.symbol();
                if (!symbol.equals("]")) {
                    throw new IllegalArgumentException("Current token is not ]");
                }
                writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                jt.advance();
            }
        } catch (IllegalArgumentException e) {
        }

        // =
        String symbol = jt.symbol();
        if (!symbol.equals("=")) {
            return;
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // expression
        compileExpression();

        // ;
        symbol = jt.symbol();
        if (!symbol.equals(";")) {
            return;
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        writer.write("</letStatement>\n");
    }

    /// method that compiles an if statement, possibly with a trailing else clause
    private void compileIf() throws IOException {
        writer.write("<ifStatement>\n");

        // if keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("if")) {
            throw new IllegalArgumentException("Current token is not if");
        }
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // (
        String symbol = jt.symbol();
        if (!symbol.equals("(")) {
            throw new IllegalArgumentException("Current token is not (");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // expression
        compileExpression();

        // )
        symbol = jt.symbol();
        if (!symbol.equals(")")) {
            throw new IllegalArgumentException("Current token is not )");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // {
        symbol = jt.symbol();
        if (!symbol.equals("{")) {
            throw new IllegalArgumentException("Current token is not {");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // statements
        compileStatements();

        // }
        symbol = jt.symbol();
        if (!symbol.equals("}")) {
            throw new IllegalArgumentException("Current token is not }");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // maybe else
        try {
            keyword = jt.keyWord();
            if (!keyword.equals("else")) {
                writer.write("</ifStatement>\n");
                return;
            }
            writer.write(String.format("<keyword>%s</keyword>\n", keyword));
            jt.advance();

            // {
            symbol = jt.symbol();
            if (!symbol.equals("{")) {
                throw new IllegalArgumentException("Current token is not {");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance();

            // statements
            compileStatements();

            // }
            symbol = jt.symbol();
            if (!symbol.equals("}")) {
                throw new IllegalArgumentException("Current token is not }");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance();
        } catch (IllegalArgumentException e) {
        }
        writer.write("</ifStatement>\n");
    }

    /// method that compiles a while statement
    private void compileWhile() throws IOException {
        writer.write("<whileStatement>\n");

        // while keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("while")) {
            throw new IllegalArgumentException("Current token is not while");
        }
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // (
        String symbol = jt.symbol();
        if (!symbol.equals("(")) {
            throw new IllegalArgumentException("Current token is not (");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // expression
        compileExpression();

        // )
        symbol = jt.symbol();
        if (!symbol.equals(")")) {
            throw new IllegalArgumentException("Current token is not )");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // {
        symbol = jt.symbol();
        if (!symbol.equals("{")) {
            throw new IllegalArgumentException("Current token is not {");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        // statements
        compileStatements();

        // }
        symbol = jt.symbol();
        if (!symbol.equals("}")) {
            throw new IllegalArgumentException("Current token is not }");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        writer.write("</whileStatement>\n");
    }

    /// method that compiles a do statement
    public void compileDo() throws IOException {
        writer.write("<doStatement>\n");

        // do keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("do")) {
            throw new IllegalArgumentException("Current token is not do");
        }
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // subroutineCall
        compileSubroutineCall();

        // ;
        String symbol = jt.symbol();
        if (!symbol.equals(";")) {
            throw new IllegalArgumentException("Current token is not ;");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        writer.write("</doStatement>\n");
    }


    /// method that compiles a return statement
    private void compileReturn() throws IOException {
        writer.write("<returnStatement>\n");

        // return keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("return")) {
            throw new IllegalArgumentException("Current token is not return");
        }
        writer.write(String.format("<keyword>%s</keyword>\n", keyword));
        jt.advance();

        // maybe expression
        if (!jt.stringVal().equals(";")) {
            compileExpression();
        }

        // ;
        String symbol = jt.symbol();
        if (!symbol.equals(";")) {
            throw new IllegalArgumentException("Current token is not ;");
        }
        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
        jt.advance();

        writer.write("</returnStatement>\n");
    }

    /// Compiles an expression list
    private void compileExpressionList() throws IOException {
        writer.write("<expressionList>\n");
        if (jt.stringVal().equals(")")) {
            writer.write("</expressionList>\n");
            return;
        }
        compileExpression();
        try {
            while (true) {
                try {
                    String symbol = jt.symbol();
                    if (!symbol.equals(",")) {
                        break;
                    }
                    writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                    jt.advance();

                    compileExpression();
                } catch (IllegalArgumentException e) {
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
        }
        writer.write("</expressionList>\n");
    }

    /// Compiles an expression.
    private void compileExpression() throws IOException {
        writer.write("<expression>\n");

        // term
        compileTerm();

        // maybe more term
        while (true) {
            try {
                String symbol = jt.symbol();
                if (!"+-*/&|<>=".contains(symbol)) {
                    break;
                }
                switch (symbol) {
                    case "<":
                        writer.write("<symbol>&lt;</symbol>\n");
                        break;
                    case ">":
                        writer.write("<symbol>&gt;</symbol>\n");
                        break;
                    case "&":
                        writer.write("<symbol>&amp;</symbol>\n");
                        break;
                    default:
                        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                }
                jt.advance();

                compileTerm();
            } catch (IllegalArgumentException e) {
                break;
            }
        }

        writer.write("</expression>\n");
    }

    /// Compiles a term. 
    private void compileTerm() throws IOException {
    
        try {
            String tokenType = jt.tokenType();
            writer.write("<term>\n");
            switch (tokenType) {
                case "integerConstant":
                    writer.write(String.format("<integerConstant>%s</integerConstant>\n", jt.stringVal()));
                    jt.advance();
                    break;
                case "stringConstant":
                    writer.write(String.format("<stringConstant>%s</stringConstant>\n", jt.stringVal().substring(1).substring(0, jt.stringVal().length() - 2)));
                    jt.advance();
                    break;
                case "keyword":
                    if ("true false null this".contains(jt.keyWord())) {
                        writer.write(String.format("<keyword>%s</keyword>\n", jt.keyWord()));
                        jt.advance();
                    }
                    break;
                case "identifier":
                    String identifier = jt.identifier();
                    writer.write(String.format("<identifier>%s</identifier>\n", identifier));
                    jt.advance();
                    if (jt.symbol().equals("[")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", jt.symbol()));
                        jt.advance();
                        compileExpression();
                        writer.write(String.format("<symbol>%s</symbol>\n", jt.symbol())); // should be ']'
                        jt.advance();
                    } else if (jt.symbol().equals("(") || jt.symbol().equals(".")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", jt.symbol()));
                        jt.advance();
                        compileSubroutineCall(); // Assume this starts right after identifier
                    }
                    break;
                case "symbol":
                    String symbol = jt.symbol();
                    if (symbol.equals("(")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                        jt.advance();
                        compileExpression();
                        writer.write(String.format("<symbol>%s</symbol>\n", jt.symbol())); // should be ')'
                        jt.advance();
                    } else if (symbol.equals("~") || symbol.equals("-")) {
                        writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                        jt.advance();
                        compileTerm();
                    }
                    break;
            }
            writer.write("</term>\n");
        } catch (IllegalArgumentException e) {
            System.err.println("Error compiling term: " + e.getMessage());
        }
    }


    /// Compiles a subroutine call
    private void compileSubroutineCall() throws IOException {
        try {
            // subroutineName | className | varName
            String identifier = jt.identifier();
            writer.write(String.format("<identifier>%s</identifier>\n", identifier));
            jt.advance();

            // maybe more
            try {
                String symbol = jt.symbol();
                if (symbol.equals(".")) {
                    writer.write(String.format("<symbol>%s</symbol>\n", symbol));
                    jt.advance();

                    // subroutineName
                    String subroutineName = jt.identifier();
                    writer.write(String.format("<identifier>%s</identifier>\n", subroutineName));
                    jt.advance();
                }
            } catch (IllegalArgumentException e) {
            }

            // (
            String symbol = jt.symbol();
            if (!symbol.equals("(")) {
                throw new IllegalArgumentException("Current token is not (");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance();

            // expressionList
            compileExpressionList();

            // )
            symbol = jt.symbol();
            if (!symbol.equals(")")) {
                throw new IllegalArgumentException("Current token is not )");
            }
            writer.write(String.format("<symbol>%s</symbol>\n", symbol));
            jt.advance();
        } catch (IllegalArgumentException e) {
        }
    }
}
