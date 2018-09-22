package cool;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.sun.corba.se.impl.orbutil.closure.Constant;

import cool.AST;
import cool.GlobalVariables;
import cool.InheritanceGraph;
import cool.ScopeTable;
import cool.ScopeTableHandler;
import jdk.nashorn.internal.objects.Global;

import java.util.HashSet;
import java.lang.StringBuilder;

class Visitor {
    /*
     * NOTE: to know about the individual visit functions Check Visitor.java
     */

    // Expression visitors

    // Used for no_expression
    public void traverse(AST.no_expr expression) {
        expression.type = "_no_type";

    }

    // Visits 'ID <- expression' expression
    public void traverse(AST.assign expression) {
        //assignment expression
        expression.e1.accept(this);

        if(expression.name.equals("self"))
        {
            GlobalVariables.errorReporter.report(GlobalVariables.filename, expression.getLineNo(), "'self' cannot be assigned");
        }
        else
        {
            String typeExpr = ScopeTableHandler.scopeTable.lookUpGlobal(expression.name);
            //attribute not present in the scope table
            if(typeExpr == null)
            {
                String errStr = new StringBuilder().append("Attribute '").append(expression.name).append("' is not defined").toString();
                GlobalVariables.errorReporter.report(GlobalVariables.filename, expression.getLineNo(), errStr);
            }
            //checking for assigment 
            else if(!UtilFunctionImpl.typeChecker(typeExpr, expression.e1.type, GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(typeExpr)),  GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(expression.e1.type))))
            {
                String errStr = new StringBuilder().append("Attribute '").append(expression.name).append("' is not consistent with type of expression").toString();
                GlobalVariables.errorReporter.report(GlobalVariables.filename, expression.getLineNo(), errStr);
            }

        }
        //all correct :-)
        expression.type = expression.e1.type;

    }

    // Visits 'expression@TYPE.ID([expression [[, expression]]∗])' expression
    public void traverse(AST.static_dispatch expression) {
        expression.caller.accept(this);

        String caller = expr.caller.type;
        for(AST.expression expr : expression.actuals)
        {
            expr.accept(this);
        }

        //return type not defined
        if(GlobalVariables.inheritanceGraph.containsClass(expression.typeid) == false)
        {
            String errString = new StringBuilder().append("Type undefined '").append(expression.typeid).append("'").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, expression.getLineNo(), errString);
        }
        else if(!UtilFunctionImpl.typeChecker(expression.typeid, caller, GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(expression.typeid)),  GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(caller))))
        {
            String errString = new StringBuilder().append("Type mismatch for caller '").append(caller).append("' expected '").append(expression.typeid + "'").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, expression.getLineNo(), errString);
            expression.type = Constants.ROOT_TYPE;
        }
        else
        {
            String methodMangled = UtilFunctionImpl.getMangledNameWithExpression(expression.typeid,  expression.actuals, expression.name);
            if(!GlobalVariables.mapMangledNames.containsKey(methodMangled))
            {
                String errString = new StringBuilder().append("Undefined method '").append(expression.name).append("' for dispatch").toString();
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, expression.getLineNo(), errString);
                expression.type = Constants.ROOT_TYPE;
            }
            else
            {
                expression.type = GlobalVariables.mapMangledNames.get(methodMangled);
            }
        }
    }

    // Visits 'expression.ID([expression [[, expression]]∗])' expression
    public void traverse(AST.dispatch expression) {
        expression.caller.accept(this);
        String callingClass = expression.caller.type;
        //class contains no method - INT or Bool
        if(GlobalVariables.inheritanceGraph.classWithNoMethodType.contains(callingClass))
        {
            GlobalVariables.errorReporter.report(Global.presentFilename, expression.getLineNo(), "Method is not defined '"+expression.name+"'");
            return;
        }

        for(AST.expression e : expression.actuals)
            e.accept(this);
        
        String methodMangled = UtilFunctionImpl.getMangledNameWithExpression(callingClass, expression.actuals, expression.name);
        
        String typeMethod;
        if(!GlobalVariables.mapMangledNames.containsKey(methodMangled))
        {
            typeMethod = null;
            while(typeMethod == null)
            {
                callingClass = GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(callingClass)).getParent().getASTClass().name;
                if(callingClass != null)
                {
                    methodMangled = UtilFunctionImpl.getMangledNameWithExpression(callingClass, expression.actuals, expression.name);
                    
                    if(GlobalVariables.mapMangledNames.containsKey(methodMangled))
                    {
                       typeMethod = GlobalVariables.mapMangledNames.get(methodMangled);
                    }
                }
                else
                    break;

            }
        }
        else
        {
            typeMethod = GlobalVariables.mapMangledNames.get(methodMangled);
        }
        if(typeMethod == null)
        {
            // method is not found
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, expression.getLineNo(), "Method signature undefined for '" + expression.name + "'");
            // to run without errors
            expression.type = Constants.ROOT_TYPE;

        }
        else{
            expression.type = typeMethod;
        }

    }

    // Visits 'if expression then expression else expression fi' expression
    public void traverse(AST.cond expression) {

        expression.predicate.accept(this);
        expression.ifbody.accept(this);
        expression.elsebody.accept(this);

        if(expression.predicate.type.equals(Constants.BOOL_TYPE) == false)
        {
            GlobalVariables.errorReporter.report(GlobalVaribles.presentFilename, expression.getLineNo(), "Return type of condtion predicate is not BOOL ");
        }

        
        expression.type = UtilFunctionImpl.joinTypesOf(expression.ifbody.type, expression.elsebody.type, GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(expression.ifbody.type)),  GlobalVariables.inheritanceGraph.get(GlobalVariables.inheritanceGraph.giveClassIndex(expression.elsebody.type)));
        
    }

    // Visits 'while expression loop expression pool' expression
    public void traverse(AST.loop expression) {
    }

    // Visits '{ [expression{}]+ }' expression
    public void traverse(AST.block expression) {
    }

    // Visits 'let ID : TYPE [<-expression] in expression' expression
    // NOTE: muliple ID declaration is converted to nested let by parser
    public void traverse(AST.let expression) {
    }

    // Visits 'case expression of [ID : TYPE => expression{}]+ esac' expression
    public void traverse(AST.typcase expression) {
    }

    // Visits 'ID : TYPE => expression{}'
    // This is not an expression, but used inside case
    public void traverse(AST.branch br) {
    }

    // Visits 'new TYPE' expression
    public void traverse(AST.new_ expression) {
    }

    // Visits 'isvoid expression' expression
    public void traverse(AST.isvoid expression) {
    }

    // Visits 'expression + expression' expression
    public void traverse(AST.plus expression) {
    }

    // Visits 'expression - expression' expression
    public void traverse(AST.sub expression) {
    }

    // Visits 'expression * expression' expression
    public void traverse(AST.mul expression) {
    }

    // Visits 'expression / expression' expression
    public void traverse(AST.divide expression) {
    }

    // Visits 'not expression' expression
    public void traverse(AST.comp expression) {
    }

    // Visits 'expression < expression' expression
    public void traverse(AST.lt expression) {
    }

    // Visits 'expression <= expression' expression
    public void traverse(AST.leq expression) {
    }

    // Visits 'expression = expression' expression
    public void traverse(AST.eq expression) {
    }

    // Visits '~expression' expression
    public void traverse(AST.neg expression) {
    }

    // Visits 'ID' expression
    public void traverse(AST.object expression) {
    }

    // Visits integer expression
    public void traverse(AST.int_const expression) {
    }

    // Visits string expression
    public void traverse(AST.string_const expression) {
    }

    // Visits bool expression
    public void traverse(AST.bool_const expression) {
    }

    public void traverse(AST.program prog) {

        // preparing inheritance graph
        GlobalVariables.inheritanceGraph = new InheritanceGraph();
        ScopeTableHandler.scopeTable = new ScopeTable<>();

        for (AST.class_ cl : prog.classes) {
            GlobalVariables.presentFilename = cl.filename;
            GlobalVariables.inheritanceGraph.addNewClass(cl);
        }

        GlobalVariables.inheritanceGraph.checkRestrictedInheritance();
        GlobalVariables.inheritanceGraph.connectGraph();
        GlobalVariables.inheritanceGraph.checkMain();
        for (int i = 0; i < GlobalVariables.inheritanceGraph.inheritanceGraph.size(); i++) {
            System.out.println(GlobalVariables.inheritanceGraph.inheritanceGraph.get(i).getASTClass().name);
        }
        GlobalVariables.inheritanceGraph.detectCycle();
        if (GlobalVariables.inheritanceGraph.getHasCycle()) {
            ArrayList<String> cycleClass = GlobalVariables.inheritanceGraph.getCyclicClass();
            for (int i = 0; i < cycleClass.size(); i++) {
                String errStr = new StringBuilder().append("Cyclic Inheritance found for class '")
                        .append(cycleClass.get(i)).append("' and its ancestors").toString();
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, 0, errStr);
            }

        } else
            System.out.println("Has no cycle");

        manglingNames();

        UtilFunctionImpl.checkForMethodRedination();

        DFSVisitor(GlobalVariables.inheritanceGraph.getRootNode());

    }

    private void DFSVisitor(GraphNode node) {
        ScopeTableHandler.scopeTable.enterScope();

        node.getASTClass().accept(this);

        for (GraphNode children : node.getChildren()) {
            DFSVisitor(children);
        }

        ScopeTableHandler.scopeTable.exitScope();

    }

    // updating our mapMangledNames
    private void manglingNames() {
        for (GraphNode tempNode : GlobalVariables.inheritanceGraph.getNodeList()) {
            AST.class_ newClass = tempNode.getASTClass();
            for (AST.feature newfeature : newClass.features) {
                if (newfeature instanceof AST.method) {
                    AST.method m = (AST.method) newfeature;
                    System.out.println("name = " + m.name);
                    String mangeledName = UtilFunctionImpl.getMangledNameWithClass(1, newClass.name, m.formals, m.name);
                    System.out.println(mangeledName);
                    System.out.println(m.typeid);
                    GlobalVariables.mapMangledNames.put(mangeledName, m.typeid);

                }
                System.out.println("dffdf");
            }
        }

        for (String key : GlobalVariables.mapMangledNames.keySet()) {
            System.out.println(key);
        }
    }

    public void traverse(AST.class_ cl) {
        GlobalVariables.presentClass = cl.name;

        // checking all its features
        for (AST.feature feature : cl.features) {
            // checking for variable
            if (feature instanceof AST.attr) {
                AST.attr variable = (AST.attr) feature;
                UtilFunctionImpl.AttrChecker(variable);
            }
            // checking for method
            else {
                AST.method method = (AST.method) feature;
                UtilFunctionImpl.MethodChecker(method);
            }
        }

        // checking the conditions for the main class
        if (Constants.MAIN_TYPE.equals(cl.name)) {
            String searchMain = ScopeTableHandler.scopeTable
                    .lookUpLocal(ScopeTableHandler.getMangledNameFunction("main"));
            System.out.println("TTHEE MAINAIN I : " + searchMain);
            if (searchMain == null) {
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, cl.getLineNo(),
                        "'main' method missing");
            } else if (UtilFunctionImpl.FuncHasArguments(searchMain)) {
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, cl.getLineNo(),
                        "'main' method cannot have arguments");
            }

        }

        if (UtilFunctionImpl.DefaultClass(cl.name)) {
            // no need to have semantic checks for default class :-)
            return;
        }

        for (AST.feature feature : cl.features) {
            feature.accept(this);
        }

    }

    // Visits the attributes of the class
    public void traverse(AST.attr attribute) {
        if (attribute.name.equals("self")) {
            ScopeTableHandler.scopeTable.remove(ScopeTableHandler.getMangledNameVar(attribute.name));
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, attribute.getLineNo(),
                    "Attribute with name 'self' cannot be defined.");

        //attribute.value.accept(this);

        } else if (!GlobalVariables.inheritanceGraph.containsClass(attribute.typeid)) {
            String errString = new StringBuilder().append("Attribute '").append(attribute.name).append("' type '")
                    .append(attribute.typeid).append("' has not been defined.").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, attribute.getLineNo(), errString);

            // For this case, we set the type id of attribute to ROOT_TYPE
            // this is done to continue compilation
            ScopeTableHandler.insertVar(attribute.name, Constants.ROOT_TYPE);

            //attribute.value.accept(this);

        } else {

           // attribute.value.accept(this);
           // check for no expression here------------------------

        }

    }

    // Visits the method of the class
    public void traverse(AST.method method) {
        // a new scope, as local variables in a function hides the scope of the
        // member variables of the class
        ScopeTableHandler.scopeTable.enterScope();

        // storing all the formals in an Array List for easy checking
        ArrayList<String> formalNames = new ArrayList<>();
        // iterating over all formals
        for (AST.formal f : method.formals) {
            if (f.name.equals("self")) {
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, f.getLineNo(),
                        "'Self' name used to define a formal.");
            }
            if (!formalNames.contains(f.name)) {
                // adding the formal names to the array list if not present
                formalNames.add(f.name);
            } else {
                // usage of same formal name is done multiple times
                String errString = new StringBuilder().append("Formal '").append(f.name)
                        .append("' has been reclared in the method '").append(method.name).append("'").toString();
                GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, method.getLineNo(), errString);

            }
            f.accept(this);

        }

        //method.body.accept(this);

        // write isconforming function here-----------------------------

        ScopeTableHandler.scopeTable.exitScope();

    }

    // Visits the formals of the method
    public void traverse(AST.formal f) {
        if (!GlobalVariables.inheritanceGraph.containsClass(f.typeid)) {
            // type has not been defined
            String errString = new StringBuilder().append("Type '").append(f.typeid).append("' of formal '")
                    .append(f.name).append("'has not been defined ").toString();
            GlobalVariables.errorReporter.report(GlobalVariables.presentFilename, f.getLineNo(), errString);
        } else {
            // the type is valid and can be inserted in our scope Table
            ScopeTableHandler.insertVar(f.name, f.typeid);
        }
    }

}