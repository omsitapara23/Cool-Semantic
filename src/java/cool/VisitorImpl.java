package cool;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import cool.GlobalVariables;
import cool.InheritanceGraph;

import java.util.HashSet;
import java.lang.StringBuilder;

class Visitor {
    /* NOTE: to know about the individual visit functions
             Check Visitor.java 
    */

    public void traverse(AST.class_ cl){}

    // Visits the attributes of the class
    public void traverse(AST.attr at){}

    // Visits the method of the class
    public void traverse(AST.method mthd){}

    // Visits the formals of the method
    public void traverse(AST.formal fm){}

    // Expression visitors

    // Used for no_expression
    public void traverse(AST.no_expr expr){}

    // Visits 'ID <- expr' expression
    public void traverse(AST.assign expr){}

    // Visits 'expr@TYPE.ID([expr [[, expr]]∗])' expression
    public void traverse(AST.static_dispatch expr){}

    // Visits 'expr.ID([expr [[, expr]]∗])' expression
    public void traverse(AST.dispatch expr){}

    // Visits 'if expr then expr else expr fi' expression
    public void traverse(AST.cond expr){}

    // Visits 'while expr loop expr pool' expression
    public void traverse(AST.loop expr){}

    // Visits '{ [expr{}]+ }' expression
    public void traverse(AST.block expr){}

    // Visits 'let ID : TYPE [<-expr] in expr' expression
    // NOTE: muliple ID declaration is converted to nested let by parser
    public void traverse(AST.let expr){}

    // Visits 'case expr of [ID : TYPE => expr{}]+ esac' expression
    public void traverse(AST.typcase expr){}

    // Visits 'ID : TYPE => expr{}'
    // This is not an expression, but used inside case
    public void traverse(AST.branch br){}

    // Visits 'new TYPE' expression
    public void traverse(AST.new_ expr){}

    // Visits 'isvoid expr' expression
    public void traverse(AST.isvoid expr){}

    // Visits 'expr + expr' expression
    public void traverse(AST.plus expr){}

    // Visits 'expr - expr' expression
    public void traverse(AST.sub expr){}
    
    // Visits 'expr * expr' expression
    public void traverse(AST.mul expr){}
    
    // Visits 'expr / expr' expression
    public void traverse(AST.divide expr){}
    
    // Visits 'not expr' expression
    public void traverse(AST.comp expr){}
    
    // Visits 'expr < expr' expression
    public void traverse(AST.lt expr){}
    
    // Visits 'expr <= expr' expression
    public void traverse(AST.leq expr){}
    
    // Visits 'expr = expr' expression
    public void traverse(AST.eq expr){}
    
    // Visits '~expr' expression
    public void traverse(AST.neg expr){}
    
    // Visits 'ID' expression
    public void traverse(AST.object expr){}
    
    // Visits integer expression
    public void traverse(AST.int_const expr){}
    
    // Visits string expression
    public void traverse(AST.string_const expr){}
    
    // Visits bool expression
    public void traverse(AST.bool_const expr){}

    public void traverse(AST.program prog) {

        // preparing inheritance graph
        GlobalVariables.inheritanceGraph = new InheritanceGraph();
        for(AST.class_ cl: prog.classes) {
            GlobalVariables.presentFilename = cl.filename;
            GlobalVariables.inheritanceGraph.addNewClass(cl);
        }

        GlobalVariables.inheritanceGraph.checkRestrictedInheritance();
        GlobalVariables.inheritanceGraph.connectGraph();
        GlobalVariables.inheritanceGraph.checkMain();
        for(int i = 0; i < GlobalVariables.inheritanceGraph.inheritanceGraph.size(); i++)
        {
            System.out.println(GlobalVariables.inheritanceGraph.inheritanceGraph.get(i).getASTClass().name);
        }
        GlobalVariables.inheritanceGraph.detectCycle();
        if(GlobalVariables.inheritanceGraph.getHasCycle())
        {
            String errStr = new StringBuilder().append("Cyclic Inheritance found").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, 0, errStr);
        }
        else
            System.out.println("Has no cycle");

        manglingNames();

    }

    // updating our mapMangledNames
    private void manglingNames()
    {
        for(GraphNode tempNode : GlobalVariables.inheritanceGraph.getNodeList())
        {
            AST.class_ newClass = tempNode.getASTClass();
            for(AST.feature newfeature : newClass.features)
            {
                if(newfeature instanceof AST.method)
                {
                    AST.method m = (AST.method) newfeature;
                    System.out.println("name = "+ m.name);
                    String mangeledName = InheritanceGraph.getMangledNameWithClass(1, newClass.name, m.formals, m.name);
                    System.out.println(mangeledName);
                    System.out.println(m.typeid);
                    GlobalVariables.mapMangledNames.put(mangeledName, m.typeid);
                    
                }
                System.out.println("dffdf");
            }
        }

        for(String key : GlobalVariables.mapMangledNames.keySet())
            {
                System.out.println(key);
            }
    }

}