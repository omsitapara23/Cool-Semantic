package cool;

import java.util.*;
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

    static
    {
        presentClass = "";
        presentFilename = "";
    }
}