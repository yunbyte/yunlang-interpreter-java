/**
 * Type of ASTNode
 */
public enum ASTNodeType {
    Program, // program entry, the root node

    IntDeclaration, // integer variable declaration
    ExpressionStmt, // expression statement, followed by a semicolon
    AssignmentStmt, // assignment statement

    Primary, // basic expression
    Multiplicative, // multiplication expression
    Additive, // addition expression

    Identifier, // identifier
    IntLiteral // integer literal
}