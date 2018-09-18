package cool;

import java.util.*;
import java.cool.ScopeTableHandler;
import java.lang.StringBuilder;

public class GlobalVariables 
{
    // stores the current filename of the class
    public static String presentFilename;

    // Our inheritance graph formed after parsing all the classes.
    public static InheritanceGraph inheritanceGraph;

    //denotes the presentClass while visiting AST classes
    public static String presentClass;

    // reports an error
    public static ErrorReporter errorReporter;

    // mapping our function mangled names with its types
    public static Map<String, String> mapMangledNames;

    static
    {
        presentClass = "";
        presentFilename = "";
        mapMangledNames = new HashMap<>();
    }
}