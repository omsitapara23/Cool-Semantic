package cool;


import java.util.*;
import java.lang.StringBuilder;

public class ScopeTableHandler
{
    //this class handels the scope table managment.
    public static ScopeTable<String> scopeTable;

    public static String getMangledNameVar(String name)
    {
        String newVar = new StringBuilder().append("VR_").append(name).toString();
        return newVar;
    }

    public static String getMangledNameFunction(String name)
    {
        String newVar = new StringBuilder().append("FN_").append(name).toString();
        return newVar;
    }

    public static String getMangledNameExpression(String name)
    {
        String newVar = new StringBuilder().append("EX_").append(name).toString();
        return newVar;
    }

    public static String getMangledNameBranch(String name)
    {
        String newVar = new StringBuilder().append("BR_").append(name).toString();
        return newVar;
    }

    public static String getMangledParamsAndReturnType(List<AST.formal> formals, String retType)
    {
        StringBuilder mangledName = new StringBuilder();
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
        mangledName.append("_");
        mangledName.append(retType);
        return mangledName.toString();

    }



    public static void insertVar(String name, String type)
    {
        String mangledVar = getMangledNameVar(name);
        scopeTable.insert(mangledVar, type);
    }

    public static void insertFunc(String name, String retType, List<AST.formal> formals)
    {
        String mangledFunc = getMangledNameFunction(name);
        String mangledParams = getMangledParamsAndReturnType(formals, retType);
        scopeTable.insert(mangledFunc, mangledParams);
    }

    public static void insertExpression(String name, String type)
    {
        String mangledVar = getMangledNameExpression(name);
        scopeTable.insert(mangledVar, type);
    }

    public static void insertBranch(String name, String type)
    {
        String mangledVar = getMangledNameBranch(name);
        scopeTable.insert(mangledVar, type);
    }

}