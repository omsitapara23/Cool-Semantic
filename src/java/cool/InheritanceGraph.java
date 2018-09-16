package cool;


import cool.AST.expression;
import java.util.*;



import java.lang.StringBuilder;


public class InheritanceGraph
{   

    // index of the root node
    private static final int INDEX_ROOT_CLASS = 0;
    private static AST.class_ AST_ROOT_CLASS = new AST.class_(Constants.ROOT_TYPE, null, null, new ArrayList<>(), 0);
    private static GraphNode AST_ROOT_NODE = new GraphNode(AST_ROOT_CLASS, INDEX_ROOT_CLASS);

    // our inheritance graph containining list of nodes
    public ArrayList <GraphNode> inheritanceGraph;
    private boolean containsMain;
    private Map<String, Integer> classNameToIndex;

    private boolean hasCycle = false;

    private ArrayList<Boolean> visited = new ArrayList<>();
    private ArrayList<Boolean> checker = new ArrayList<>();


    private ArrayList<String> restrictedType = new ArrayList<String>(Arrays.asList(Constants.STRING_TYPE, Constants.IO_TYPE, Constants.BOOL_TYPE, Constants.INT_TYPE));
    
    private ArrayList<String> restrictedInheritanceType = new ArrayList<String>(Arrays.asList(Constants.STRING_TYPE, Constants.BOOL_TYPE, Constants.INT_TYPE));

    public boolean getHasCycle()
    {
        return hasCycle;
    }

    public InheritanceGraph()
    {
        containsMain = false;
        classNameToIndex = new HashMap<>();
        inheritanceGraph = new ArrayList<>();
        addBasicClass();
    }

    public boolean containsMain()
    {
        return containsMain;
    }

    public GraphNode getRootNode()
    {
        return AST_ROOT_NODE;
    }

    public List<GraphNode> getNodeList()
    {
        return inheritanceGraph;
    }

    public boolean containsClass(String name)
    {
        return(classNameToIndex.containsKey(name));
    }

    private void addBasicClass()
    {
        //the object class is the root of the inheritance graph.
        //It has following methods : abort, type_name, copy.
        AST_ROOT_CLASS.features = new ArrayList<>();

        //this method abort halts the program execution with error message.
        AST_ROOT_CLASS.features.add(new AST.method("abort", new ArrayList<>(), Constants.ROOT_TYPE, null, 0));

        //this method type_name returns a string with the name of the class of the object.
        AST_ROOT_CLASS.features.add(new AST.method("type_name", new ArrayList<>(), Constants.STRING_TYPE, null, 0));

        //this method copy produces a shallow copy of the object
        AST_ROOT_CLASS.features.add(new AST.method("copy", new ArrayList<>(), Constants.ROOT_TYPE, null, 0));

        //insterting the object class
        classNameToIndex.put(Constants.ROOT_TYPE, INDEX_ROOT_CLASS);

        //adding this class as a node in the graph
        inheritanceGraph.add(AST_ROOT_NODE);




        
        //the IO has following methods : out_string, in_string, out_in, in_int
        List<AST.feature> IOFeatures = new ArrayList<>();

        // ( x : Int ) for out_int method
        // here x is the int to be printed using out_int
        List<AST.formal> intFormal = new ArrayList<>();
        intFormal.add(new AST.formal("x", Constants.INT_TYPE, 0));

        // adding method out_int 
        IOFeatures.add(new AST.method("out_int", intFormal, Constants.IO_TYPE, null, 0));

        //adding the in_string for taking int as input
        IOFeatures.add(new AST.method("in_int", new ArrayList<>(), Constants.INT_TYPE, null, 0));

        // ( x : String ) for out_string method
        // here x is the string to be printed using out_string
        List<AST.formal> stringFormal = new ArrayList<>();
        stringFormal.add(new AST.formal("x", Constants.STRING_TYPE, 0));

        // adding method out_string 
        IOFeatures.add(new AST.method("out_string", stringFormal, Constants.IO_TYPE, null, 0));

        //adding the in_string for taking string as input
        IOFeatures.add(new AST.method("in_string", new ArrayList<>(), Constants.STRING_TYPE, null, 0));


        //completed all methods adding the class
        AST.class_ IOAstClass = new AST.class_(Constants.IO_TYPE, null, Constants.ROOT_TYPE, IOFeatures, 0);
        GraphNode IONode = new GraphNode(IOAstClass, 0);

        classNameToIndex.put(Constants.IO_TYPE, inheritanceGraph.size());
        inheritanceGraph.add(IONode);




        List<AST.feature> StringFeatures = new ArrayList<>();

        // ( x : String ) for concat method
        // here x is the string to be concated using concat
        List<AST.formal> stringFormal1 = new ArrayList<>();
        stringFormal.add(new AST.formal("x", Constants.STRING_TYPE, 0));

        //adding the concat method 
        StringFeatures.add(new AST.method("concat", stringFormal1, Constants.STRING_TYPE, null, 0));

        //( x : Int, y : Int) for substr method
        // here x is the start and y is the length for sbustr
        List<AST.formal> intFormal1 = new ArrayList<>();
        intFormal.add(new AST.formal("x", Constants.INT_TYPE, 0));
        intFormal.add(new AST.formal("y", Constants.INT_TYPE, 0));

        //adding the substr method 
        StringFeatures.add(new AST.method("substr", intFormal1, Constants.STRING_TYPE, null, 0));

        //adding length function which retuns the length
        StringFeatures.add(new AST.method("length", new ArrayList<>(), Constants.INT_TYPE, null, 0));

        //adding the string class
        AST.class_ StringAstClass = new AST.class_(Constants.STRING_TYPE, null, Constants.ROOT_TYPE, StringFeatures, 0);
        GraphNode StringNode = new GraphNode(StringAstClass, 0);

        classNameToIndex.put(Constants.STRING_TYPE, inheritanceGraph.size());
        inheritanceGraph.add(StringNode);


        addIntClass();
        addBoolClass();
    }

    //Int class has no methods implemented
    private void addIntClass()
    {
        classNameToIndex.put(Constants.INT_TYPE, -1);
    }

    //Bool class does not have any methods
    private void addBoolClass()
    {
        classNameToIndex.put(Constants.BOOL_TYPE, -1);
    }


    public void addNewClass(AST.class_ newClass)
    {
        //checking for class redefination
        if(classNameToIndex.containsKey(newClass.name))
        {
            String errorStr = new StringBuilder().append("Redefination of class'").append(newClass.name).append("' found").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, newClass.getLineNo(), errorStr);
        }
        //checking for restricted names
        else if(restrictedType.contains(newClass.name))
        {
            String errorStr = new StringBuilder().append("Restricted name used for class '").append(newClass.name).append("' found").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, newClass.getLineNo(), errorStr);
        }
        //valid class adding to graph.
        else
        {
            //checking is it main or not
            if(newClass.name.equals(Constants.MAIN_TYPE))
                containsMain = true;
            
            //adding the repective class
            classNameToIndex.put(newClass.name, inheritanceGraph.size());
            GraphNode newNode = new GraphNode(newClass, inheritanceGraph.size());
            inheritanceGraph.add(newNode);
            
        }
    }

    //this function checks the restricted inheritance.
    public void checkRestrictedInheritance()
    {
        for(GraphNode currNode : inheritanceGraph)
        {
            if(currNode.getASTClass().parent != null)
            {
                if(restrictedInheritanceType.contains(currNode.getASTClass().parent))
                {
                    String errorStr = new StringBuilder().append("Restricted inheritance found for class '").append(currNode.getASTClass().name).append("'").append(classNameToIndex.get(currNode.getASTClass().name)).toString();
                    GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, currNode.getASTClass().getLineNo(), errorStr);

                }
            }
        }
    }

    public void connectGraph()
    {
        for(GraphNode currNode : inheritanceGraph)
        {
            if(currNode.getASTClass().parent == null)
            {
                //checking for root class itself
                //if not the root class then all the class inherit from the root class.
                if(!currNode.getASTClass().name.equals(Constants.ROOT_TYPE))
                {
                    currNode.setParent(AST_ROOT_NODE);
                    AST_ROOT_NODE.addChild(currNode);
                }
            }
            else
            {
                if(classNameToIndex.containsKey(currNode.getASTClass().parent))
                {
                    //adding this node as child to the parent
                    inheritanceGraph.get(classNameToIndex.get(currNode.getASTClass().parent)).addChild(currNode);
                    //setting the parent for current node
                    currNode.setParent(inheritanceGraph.get(classNameToIndex.get(currNode.getASTClass().parent)));
                }
                //no parent exist
                else
                {
                    String errorStr = new StringBuilder().append("No parent found for class '").append(currNode.getASTClass().name).append("'").toString();
                    GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, currNode.getASTClass().getLineNo(), errorStr);
                }
            }
        }
    }

    public void checkMain()
    {
        if(containsMain == false)
        {
            String errorStr = new StringBuilder().append("Main class not found ").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, 0, errorStr);
        }
    }

    public boolean cycleUtil(int i)
    {
        System.out.println("i = "+i);
        if(visited.get(i) == false)
        {
            visited.set(i,true);
            checker.set(i,true);

            List<GraphNode> temp = inheritanceGraph.get(i).getChildren();
            System.out.println("child : " + temp.size() + " Parent, : " + i);
            for(int j = 0;j<temp.size(); j++)
            {
                int currChild = temp.get(j).getIndex();
                System.out.println("child : " + currChild + " Parent, : " + i);
                if(!visited.get(currChild) && cycleUtil(currChild))
                    return true;
                if(checker.get(currChild))
                    return true;
            }
        }

        checker.set(i,false);
        return false;
    }

    public void detectCycle()
    {
        
        int size = inheritanceGraph.size();
        for(int i=0;i<size;i++)
        {
            visited.add(false);
            checker.add(false);
        }
 
        for(int i=0;i<size;i++)
        {
            if(cycleUtil(i))
                hasCycle = true;
        }
    }

    public static String getMangledNameWithExpression(String className, List<AST.expression> expressions, String functionName)
    {
        StringBuilder mangledName = new StringBuilder();

        //Adding class name to mangledName
        mangledName.append("_C");
        if(className==null)
             mangledName.append(0);
        else
            mangledName.append(className.length()).append(className);

        
        //Adding function name to mangledName
        mangledName.append("_F");
        if(functionName==null)
            mangledName.append(0);
        else
            mangledName.append(functionName.length()).append(functionName);
        
        //Adding arguments to mangledName
        if(expressions!=null)
        {
            mangledName.append("_A").append(expressions.size()).append("_");
            int total = 0;
            for(AST.expression tempexpression : expressions)
            {
                total++;
                mangledName.append(total).append("N").append(tempexpression.type.length()).append(tempexpression.type);
            }

            if(expressions.size()>0)
                mangledName.append("_FT");
            else
                mangledName.append("_FF");
                
        }

        else
        {
            mangledName.append("_AN0_FF");
        }

        mangledName.append("_");
        return mangledName.toString();
    }

    public static String getMangledNameWithClass(int condition, String name, List<AST.formal> formals, String functionName)
    {
        StringBuilder mangledName = new StringBuilder();

        // if condition == 1 , name = className else name = type
        if(condition == 1)
        {
            //Adding class name to mangledName
            mangledName.append("_C");
            if(name==null)
                mangledName.append(0);
            else
                mangledName.append(name.length()).append(name);
        }
        else
        {
            //Adding type name to mangledName
            mangledName.append("_T");
            if(name==null)
                mangledName.append(0);
            else
                mangledName.append(name.length()).append(name);
        }
        
        System.out.println(mangledName.toString());
        //Adding function name to mangledName
        mangledName.append("_F");
        if(functionName==null)
            mangledName.append(0);
        else
            mangledName.append(functionName.length()).append(functionName);
        
        //Adding arguments to mangledName
        if(formals!=null)
        {
            mangledName.append("_A").append(formals.size()).append("_");
            int total = 0;
            for(AST.formal tempFormal : formals)
            {
                total++;
                mangledName.append(total).append("N").append(tempFormal.typeid.length()).append(tempFormal.typeid);
            }

            if(formals.size()>0)
                mangledName.append("_FT");
            else
                mangledName.append("_FF");
                
        }

        else
        {
            mangledName.append("_AN0_FF");
        }
        System.out.println(mangledName.toString());
        mangledName.append("_");
        return mangledName.toString();
    }


}