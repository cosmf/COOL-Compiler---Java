(*******************************
 *** Classes Product-related ***
 *******************************)


class Product {
    name : String;
    model : String;
    price : Int;
    type : String;

    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- t;
        self;
    }};

    toString(): String {
        type.concat("(").concat(name).concat(";").concat(model).concat(")")
    };

    calcGenericPrice(): Int {
        price * 119 / 100
    };

    getprice(): Int {
        self.calcGenericPrice()
    };

};

class Edible inherits Product {
    -- VAT tax is lower for foods
    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- t;
        self;
    }};
    getprice():Int { price * 109 / 100 };
};

class Soda inherits Edible {
    -- sugar tax is 20 bani
    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- "Soda";
        self;
    }};

    getprice():Int {price * 109 / 100 + 20};

    getname():String {model};
};

class Coffee inherits Edible {
    -- this is technically poison for ants
    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- "Coffee";
        self;
    }};

    getprice():Int {price * 119 / 100};
};

class Laptop inherits Product {
    -- operating system cost included
    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- "Laptop";
        self;
    }};

    getprice():Int {price * 119 / 100 + 499};
};

class Router inherits Product {
    init(n : String, m: String, p : Int, t : String):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        type <- "Router";
        self;
    }};

};

(****************************
 *** Classes Rank-related ***
 ****************************)
class Rank {
    name : String;
    type : String;
    order : Int;

    init(n : String, t : String, o : Int): SELF_TYPE {{
        name <- n;
        type <- t;
        order <- o;
        self;
    }};

    getName(): String {
        name
    };

    toString(): String {
        type.concat("(").concat(name).concat(")")
    };

    getRankLevel(): Int {
        0 -- Default, should be overridden by subclasses
    };

    getOrder(): Int {
        order
    };
};

class Private inherits Rank {
    init(n : String, t : String, o : Int): SELF_TYPE {{
        name <- n;
        type <- "Private";
        order <- o;
        self;
    }};

    getRankLevel(): Int {
        100
    };
};

class Corporal inherits Private {
    init(n : String, t : String, o : Int): SELF_TYPE {{
        name <- n;
        type <- "Corporal";
        order <- o;
        self;
    }};

    getRankLevel(): Int {
        200
    };
};

class Sergent inherits Corporal {
    init(n : String, t : String, o : Int): SELF_TYPE {{
        name <- n;
        type <- "Sergent";
        order <- o;
        self;
    }};

    getRankLevel(): Int {
        300
    };
};

class Officer inherits Sergent {
    init(n : String, t : String, o : Int): SELF_TYPE {{
        name <- n;
        type <- "Officer";
        order <- o;
        self;
    }};

    getRankLevel(): Int {
        400
    };

};
    
    (*******************************
    *** Classes Object-related ***
    *******************************)
-- Represents a String object with a single attribute
class StringObject {
    value: String;

    init(v: String): SELF_TYPE {{
        value <- v;
        self;
    }};

    getValue(): String {
        value
    };

    toString(): String {
        "String(".concat(value).concat(")")
    };
};

-- Represents an Int object with a single attribute
class IntObject {
    value: String;

    init(v: String): SELF_TYPE {{
        value <- v;
        self;
    }};

    toString(): String {
        "Int(".concat(value).concat(")")
    };
};

-- Represents a Bool object as a String with a single attribute
class BoolObject {
    value: String;

    init(v: String): SELF_TYPE {{
        value <- v;
        self;
    }};

    toString(): String {
        "Bool(".concat(value).concat(")")
    };
};

-- Represents an IO object with no attributes
class IOObject {
    toString(): String {
        "IO()"
    };
};