import java.util.List;

/**
 * ASTNode: output of parser
 */
public interface ASTNode {
    // parent node
    public ASTNode getParent();

    // children nodes
    public List<ASTNode> getChildren();

    public ASTNodeType getType();

    public String getText();
}
