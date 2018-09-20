package cool;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cool.AST;
import cool.GlobalVariables;
import cool.InheritanceGraph;
import cool.ScopeTable;
import cool.ScopeTableHandler;

import java.util.HashSet;
import java.lang.StringBuilder;

class Visitor {
    /*
     * NOTE: to know about the individual visit functions Check Visitor.java
     */

    // Expression visitors

    // Used for no_expression
    public void traverse(AST.no_expr expr) {
    }

    // Visits 'ID <- expr' expression
    public void traverse(AST.assign expr) {
    }

    // Visits 'expr@TYPE.ID([expr [[, expr]]∗])' expression
    public void traverse(AST.static_dispatch expr) {
    }

    // Visits 'expr.ID([expr [[, expr]]∗])' expression
    public void traverse(AST.dispatch expr) {
    }

    // Visits 'if expr then expr else expr fi' expression
    public void traverse(AST.cond expr) {
    }

    // Visits 'while expr loop expr pool' expression
    public void traverse(AST.loop expr) {
    }

    // Visits '{ [expr{}]+ }' expression
    public void traverse(AST.block expr) {
    }

    // Visits 'let ID : TYPE [<-expr] in expr' expression
    // NOTE: muliple ID declaration is converted to nested let by parser
    public void traverse(AST.let expr) {
    }

    // Visits 'case expr of [ID : TYPE => expr{}]+ esac' expression
    public void traverse(AST.typcase expr) {
    }

    // Visits 'ID : TYPE => expr{}'
    // This is not an expression, but used inside case
    public void traverse(AST.branch br) {
    }

    // Visits 'new TYPE' expression
    public void traverse(AST.new_ expr) {
    }

    // Visits 'isvoid expr' expression
    public void traverse(AST.isvoid expr) {
    }

    // Visits 'expr + expr' expression
    public void traverse(AST.plus expr) {
    }

    // Visits 'expr - expr' expression
    public void traverse(AST.sub expr) {
    }

    // Visits 'expr * expr' expression
    public void traverse(AST.mul expr) {
    }

    // Visits 'expr / expr' expression
    public void traverse(AST.divide expr) {
    }

    // Visits 'not expr' expression
    public void traverse(AST.comp expr) {
    }

    // Visits 'expr < expr' expression
    public void traverse(AST.lt expr) {
    }

    // Visits 'expr <= expr' expression
    public void traverse(AST.leq expr) {
    }

    // Visits 'expr = expr' expression
    public void traverse(AST.eq expr) {
    }

    // Visits '~expr' expression
    public void traverse(AST.neg expr) {
    }

    // Visits 'ID' expression
    public void traverse(AST.object expr) {
    }

    // Visits integer expression
    public void traverse(AST.int_const expr) {
    }

    // Visits string expression
    public void traverse(AST.string_const expr) {
    }

    // Visits bool expression
    public void traverse(AST.bool_const expr) {
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