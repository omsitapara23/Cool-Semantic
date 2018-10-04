package cool;

import java.util.ArrayList;
import java.util.List;


import cool.AST;
import cool.GlobalVariables;
import cool.InheritanceGraph;
import cool.ScopeTable;
import cool.ScopeTableHandler;

import java.lang.StringBuilder;

class UtilFunctionImpl {
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

    public static void checkForMethodRedination()
    {
        for(GraphNode tempNode : GlobalVariables.inheritanceGraph.getNodeList())
        {
            AST.class_ newClass = tempNode.getASTClass();
            ArrayList<String> methods = new ArrayList<>();
            for(AST.feature newfeature : newClass.features)
            {
                if(newfeature instanceof AST.method)
                {
                    AST.method m = (AST.method) newfeature;
                    if(methods.contains(m.name))
                    {
                        String errStr = new StringBuilder().append("Redefination found for method '").append(m.name).append("' in class '").append(newClass.name).append("'").toString();
                        GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, m.getLineNo(), errStr);
                    }
                    
                    methods.add(m.name);
                    
                }
            }
        }
    }

    public static void AttrChecker(AST.attr attribute)
    {
        if(ScopeTableHandler.scopeTable.lookUpGlobal(ScopeTableHandler.getMangledNameVar(attribute.name)) == null)
        {
            //first time variable declared so no error yahooo!!
            ScopeTableHandler.insertVar(attribute.name, attribute.typeid);
        }
        // redefination found 
        else
        {
            //multiple defination in same class
            if(ScopeTableHandler.scopeTable.lookUpLocal(ScopeTableHandler.getMangledNameVar(attribute.name))!= null)
            {
                String errStr = new StringBuilder().append("Multiple defination found for Attribute '")
                                .append(attribute.name).append("' in class '").append(GlobalVariables.presentClass)
                                .append("'").toString();
                
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, attribute.getLineNo(), errStr);
                                
            }
            //already defined in one of the parent class
            else
            {
                String errStr = new StringBuilder().append("Attribute '")
                                .append(attribute.name).append("' in class '").append(GlobalVariables.presentClass)
                                .append("' already defined in one of the parent class.").toString();
                
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, attribute.getLineNo(), errStr);
            }
        }
    }

    public static void MethodChecker(AST.method method)
    {
        // if return type of the method is not defined
        //the return type can be any object to the class defined
        if(GlobalVariables.inheritanceGraph.containsClass(method.typeid) == false)
        {
            String errStr = new StringBuilder().append("Return type for method '")
                                .append(method.name).append("' in class '").append(GlobalVariables.presentClass)
                                .append("' has not been defined").toString();
                
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, method.getLineNo(), errStr);
            //assiging the return type of method as object
            method.typeid = Constants.ROOT_TYPE;
        }

        String methodMangeled = ScopeTableHandler.getMangledParamsAndReturnType(method.formals, method.typeid);
        String methodScopeName = ScopeTableHandler.scopeTable.lookUpGlobal(ScopeTableHandler.getMangledNameFunction(method.name));

        //function redefination clashing with the defination of parent class.

        if(methodScopeName != null && methodMangeled.compareTo(methodScopeName) != 0)
        {   
            String errStr = new StringBuilder().append("Method '")
                                .append(method.name).append("' in class '").append(GlobalVariables.presentClass)
                                .append("' does not match with its declaration in parent class.").toString();
                
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, method.getLineNo(), errStr);
        }

        ScopeTableHandler.insertFunc(method.name, method.typeid, method.formals);
        
    }

    public static boolean FuncHasArguments(String mangledName) 
    {
        if(mangledName==null) return false;
        String Args = mangledName.substring(mangledName.lastIndexOf("_") - 3, mangledName.lastIndexOf("_") + 1);
        System.out.println(Args);
        return "_FT_".equals(Args);
    }


    public static boolean DefaultClass(String className)
    {
        if(Constants.ROOT_TYPE.equals(className) || Constants.IO_TYPE.equals(className) 
        || Constants.STRING_TYPE.equals(className))
        {
            return true;

        }
        else 
        {
            return false;

        }
    }

    public static ArrayList<String> fillAncesstor(GraphNode g)
    {
        ArrayList <String> Ancesstors = new ArrayList<>();
        while(g.hasParent())
        {
            Ancesstors.add(g.getParent().getASTClass().name);
            g = g.getParent();
        }

        return Ancesstors;
    }


    public static String findLCA(GraphNode gtp1, GraphNode gtp2)
    {

        ArrayList <String> AncesstorA;
        ArrayList <String> AncesstorB;
        AncesstorA = fillAncesstor(gtp1);
        AncesstorB = fillAncesstor(gtp2);

        Boolean check = false;
        int i = AncesstorA.size()-1, j = AncesstorB.size()-1;
        for( ;i>=0 && j >=0 ; i--, j--)
        {
            if(AncesstorA.get(i) != AncesstorB.get(j))
                break;
        }

        return AncesstorA.get(i+1);

    }

    public static String joinTypesOf(String typeA, String typeB, GraphNode gtp1, GraphNode gtp2 )
    {
        if(typeA.equals(typeB))
            return typeA;
        else if(InheritanceGraph.restrictedInheritanceType.contains(typeA) || InheritanceGraph.restrictedInheritanceType.contains(typeB))
        {
            return Constants.ROOT_TYPE;
        }

        String LCA = findLCA(gtp1, gtp2);
        return LCA;

    }

    public static boolean typeChecker(String tp1, String tp2, GraphNode gtp1, GraphNode gtp2)
    {
        //checking if the type1 is global or both the types are equal
        if(tp1.equals(Constants.ROOT_TYPE) || tp2.equals(tp1))
        {
            return true;
        }
        //checking for assigment as : Int <- String these are semantically incorrect in cool
        else if(InheritanceGraph.restrictedInheritanceType.contains(tp1) || InheritanceGraph.restrictedInheritanceType.contains(tp2))
        {
            return false;
        }

        //checking if type1 is any parent type of type2
        while(gtp2.hasParent())
        {
            gtp2 = gtp2.getParent();
            if(gtp1.equals(gtp2))
            {
                return true;
            }
        }

        return false;
    }


}