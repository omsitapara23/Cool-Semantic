--program implements simple calculator using classes and inheritance.
--NON TRIVIAL
--These program checks most of the  rules named:
(*
    program::= [[class; ]]+
    class::= ∗class TYPE [inherits TYPE] { [[feature; ]] }
    formal::=ID : TYPE
    expr::= ID <- expr
        |   expr + expr 
        |   expr − expr
        |   expr ∗ expr
        |   expr / expr
        |   expr < expr
        |   expr <= expr
        |   expr = expr
        |   (expr)
        |   ID
        |   let ID : TYPE [ <- expr ] [[, ID : TYPE [ <- expr ]]]∗ in expr
        |   { [[expr; ]]+ }
        |   if expr then expr else expr fi


*)

class Calculator inherits Result {
    a : Int;
    b : Int;
    r : Int;
    inti(i : Int, j : Int) : Calculator {
        {
            a <- i;
            b <- j;
            r <- 0;
            self;
        }

    };

    sum() : Int {
        r <- a + b
    };

    multiplication() : Int {
        r <- a * b
    };

    subtract() : Int {
        r <- a - b
    };

    divide() : Int {
        r <- a/b
    };

    result(i : Int) : Result {
        (new Result).init(i, self)
    }; 

    return() : Int {
        r
    };

};

class Result inherits Calculator {

    d : Int;
    calculatorObj : Calculator;
    init(i : Int, obj : Calculator) : Result {
       {
         d <- i;
         calculatorObj <- obj;
         self;
       } 
    };

    calculate() : Int {
        if d = 1 then calculatorObj.sum() 
        else if d = 2 then calculatorObj.multiplication()
        else if d = 3 then calculatorObj.divide()
        else  calculatorObj.subtract()
        fi fi fi
    };
};

class Main inherits IO {
    myCalculator : Calculator;
    myResult : Result;
    main() : Object {{
        out_string("Enter two numbers \n");
        let i : Int <- in_int() in 
        let j : Int <- in_int() in 
        myCalculator <- new Calculator.inti(i, j);
        out_string("Enter 1 for addition \n");
        out_string("Enter 2 for multiplication \n");
        out_string("Enter 3 for division \n");
        out_string("Enter 4 for subtraction \n");
        let k : Int <- in_int() in 
        if 4 < k then out_string ("Wrong choice \n")
        else
        {
          myResult <- myCalculator.result(k);
          out_int(myResult.calculate());
          out_string("\n");
        } fi;     
    }};

};