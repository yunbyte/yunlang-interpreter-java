import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleParser {
    public static void main(String[] args) {
        SimpleParser parser = new SimpleParser();
        String script = null;
        ASTNode tree = null;

        try {
            script = "int age = 45+2; age= 20; age+10*2;";
            System.out.println("解析：" + script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        // 测试异常语法
        try {
            script = "2+3+;";
            System.out.println("解析：" + script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 测试异常语法
        try {
            script = "2+3*;";
            System.out.println("解析：" + script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Execute script and print all AST node and calculating process.
     * 
     * @param script
     */
    public void execute(String script) {
        try {
            ASTNode tree = parse(script);

            dumpAST(tree, "");
            evaluate(tree, "");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Parse script and return root AST node.
     * 
     * @param script
     * @return
     */
    private ASTNode parse(String script) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        ASTNode rootNode = prog(tokens);
        return rootNode;
    }

    /**
     * Evaluate AST node and print the evaluation process.
     * 
     * @param node
     * 
     * @param indent
     * @return
     */
    private int evaluate(ASTNode node, String indent) {
        int result = 0;
        System.out.println(indent + "Calculating: " + node.getType());
        switch (node.getType()) {
            case Program:
                for (ASTNode child : node.getChildren()) {
                    result = evaluate(child, indent + "\t");
                }
                break;
            case Additive:
                ASTNode child1 = node.getChildren().get(0);
                int value1 = evaluate(child1, indent + "\t");
                ASTNode child2 = node.getChildren().get(1);
                int value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("+")) {
                    result = value1 + value2;
                } else {
                    result = value1 - value2;
                }
                break;
            case Multiplicative:
                child1 = node.getChildren().get(0);
                value1 = evaluate(child1, indent + "\t");
                child2 = node.getChildren().get(1);
                value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("*")) {
                    result = value1 * value2;
                } else {
                    result = value1 / value2;
                }
                break;
            case IntLiteral:
                result = Integer.valueOf(node.getText()).intValue();
            default:
        }

        System.out.println(indent + "Result: " + result);
        return result;
    }

    /**
     * Integer variable declaration statement
     * 
     * intDeclaration : Int Identifier ('=' additiveExpression)?;
     * int a;
     * int b = 2;
     * int c = 2+3;
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode intDeclare(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek(); // pre-read
        if (token != null && token.getType() == TokenType.Int) { // match int
            tokens.read(); // remove int
            token = tokens.peek(); // pre-read
            if (token != null && token.getType() == TokenType.Identifier) { // match Identifier
                token = tokens.read(); // read Identifier value
                // create a ASTNode, type is IntDeclaration, text is Identifier value of token
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());

                token = tokens.peek();
                if (token != null && token.getType() == TokenType.Assignment) { // match =
                    tokens.read(); // read =
                    // create a ASTNode, type is IntDeclaration, text is Identifier value of token
                    SimpleASTNode child = additive(tokens);
                    if (child != null) {
                        node.addChild(child);
                    } else {
                        throw new Exception("invalid variable initialization, expecting an expression");
                    }
                }
            } else {
                throw new Exception("variable name expected");
            }

            if (node != null) {
                token = tokens.peek();
                if (token != null && token.getType() == TokenType.SemiColon) {
                    tokens.read();
                } else {
                    throw new Exception("invalid statement, expecting semicolon");
                }
            }
        }

        return node;
    }

    /**
     * Syntax analysis: root node
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private ASTNode prog(TokenReader tokens) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Program, "Calculator");
        SimpleASTNode child = additive(tokens);
        if (node != null) {
            node.addChild(child);
        }

        return node;
    }

    /**
     * Syntax analysis: addition expression
     * * additiveExpression
     * : multiplicativeExpression
     * | multiplicativeExpression Plus additiveExpression
     * ;
     * 
     * add -> mul add'
     * add' -> + mul add' | ε
     * 
     * add -> mul (+ mul)*
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode additive(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = multiplicative(tokens); // parse the first node
        SimpleASTNode node = child1; // return the first node if no second node

        if (child1 != null) {
            while (true) { // add'
                Token token = tokens.peek();
                if (token != null && (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus)) {
                    token = tokens.read();
                    SimpleASTNode child2 = multiplicative(tokens); // next node
                    if (child2 != null) {
                        node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                        node.addChild(child1);
                        node.addChild(child2);
                        child1 = node;
                    } else {
                        throw new Exception("invalid additive expression, expecting the right part.");
                    }
                } else {
                    break;
                }
            }
        }

        return node;
    }

    /**
     * Syntax analysis: multiplication expression
     * multiplicativeExpression
     * : IntLiteral
     * | IntLiteral Star multiplicativeExpression
     * ;
     * 
     * mul -> pri * mul'
     * mul' -> * pri mul' | ε
     * 
     * mul -> pri (*mul)*
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = primary(tokens); // parse the first node
        SimpleASTNode node = child1; // return the first node if no second node

        while (true) {
            Token token = tokens.peek();
            if (token != null && (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
                token = tokens.read();
                SimpleASTNode child2 = primary(tokens);
                if (child2 != null) {
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                    child1 = node;
                } else {
                    throw new Exception("invalid multiplicative expression, expecting the right part.");
                }
            } else {
                break;
            }
        }

        return node;
    }

    /**
     * Syntax analysis: basic expression
     * IntLiteral
     * 
     * @param tokens
     * @return
     * @throws Exception
     */
    private SimpleASTNode primary(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if (token != null) {
            if (token.getType() == TokenType.IntLiteral) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            } else if (token.getType() == TokenType.Identifier) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            } else if (token.getType() == TokenType.LeftParen) {
                token = tokens.read();
                node = additive(tokens);
                if (node != null) {
                    token = tokens.peek();
                    if (token != null && token.getType() == TokenType.RightParen) {
                        tokens.read();
                    } else {
                        throw new Exception("expecting right parenthesis");
                    }
                } else {
                    throw new Exception("expecting an additive expression inside parenthesis.");
                }
            }

        }

        return node;
    }

    /**
     * Simple implemention for ASTNode
     */
    private class SimpleASTNode implements ASTNode {
        SimpleASTNode parent = null;
        List<ASTNode> children = new ArrayList<ASTNode>();
        List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
        ASTNodeType nodeType = null;
        String text = null;

        public SimpleASTNode(ASTNodeType nodeType, String text) {
            this.nodeType = nodeType;
            this.text = text;
        }

        @Override
        public ASTNode getParent() {
            return this.parent;
        }

        @Override
        public List<ASTNode> getChildren() {
            return readonlyChildren;
        }

        @Override
        public ASTNodeType getType() {
            return nodeType;
        }

        @Override
        public String getText() {
            return text;
        }

        public void addChild(SimpleASTNode child) {
            children.add(child);
            child.parent = this;
        }

    }

    /**
     * Print tree struct for ASTNode
     * 
     * @param node
     * @param indent tab
     */
    private void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }
}
