package cool;

import java.util.ArrayList;
import java.util.List;

import cool.AST;
import cool.GlobalVariables;

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

}