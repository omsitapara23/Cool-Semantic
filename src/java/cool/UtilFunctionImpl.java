package cool;

import java.util.ArrayList;
import java.util.List;


import cool.AST;
import cool.GlobalVariables;
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


}