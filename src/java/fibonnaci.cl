--function to give nth fibonacci number
--TRIVIAL

class Main inherits IO {
    main (): Object {{
        let input: Int <- in_int() in
        out_string("Fibonacci Number is \n ").out_int(fibonacci(input));   --dispatching method
        out_string("\n");
    }};

    fibonacci(num : Int): Int {
        if num = 1 then 1       -- base condition
        else if num = 2 then 1  -- base condition
        else fibonacci(num - 1) + fibonacci(num - 2)   -- recursive calls
        fi fi 
    };
};