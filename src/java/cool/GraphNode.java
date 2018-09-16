package cool;

import java.util.*;
public class GraphNode
{
    //parent is not defined
    public static final boolean PARENT_UNDEFINED;

    private AST.class_ astClass;
    private GraphNode parent;
    private int index;
    private List<GraphNode> childrenList;
    private boolean initiated;


    //static initializer
    static
    {
        PARENT_UNDEFINED = true;
    }

    public GraphNode(AST.class_ astClass, int index)
    {
        this.initiated = false;
        initialize(astClass, index);
    }

    private void initialize(AST.class_ astClass, int index)
    {
        if(initiated)
            return;
        this.astClass = astClass;
        this.index = index;
        this.childrenList = new ArrayList<>();
        this.parent = null;
        this.initiated = true;
    }

    public void addChild(GraphNode newChild)
    {
        childrenList.add(newChild);
    }

    public boolean hasParent()
    {
        if(parent != null)
            return true;
        else 
            return false; 
    }

    public GraphNode getParent()
    {
        return parent;
    }

    public AST.class_ getASTClass()
    {
        return astClass;
    }

    public int getIndex()
    {
        return index;
    }

    public List<GraphNode> getChildren()
    {
        return childrenList;
    }

    public void setParent(GraphNode parent)
    {
        this.parent = parent;
    }

    public boolean equalTo(GraphNode newNode)
    {
        return this.index == newNode.getIndex();
    }

}
