class List inherits IO {
    -- Abstract class representing a linked list

    hd(): Object {{ abort(); 0; }};  -- Returns the head of the list (abstract)
    tl(): List {{ abort(); self; }};    -- Returns the tail of the list (abstract)
    isEmpty(): Bool { true };  -- Check if the list is empty

    cons(h: Object): List {
        new Cons.init(h, self) -- Create a new `Cons` with the given head and this list as the tail
    };

    get(index: Int): Object {{ abort(); 0;}};  -- Abstract get method (returns Object)

    getList(index: Int): List {
        -- In Nil or base class, returning itself indicates an invalid index.
        self
    };

    flattenList(): List {
        -- In Nil or base class, returning itself indicates an empty list.
        self
    };

    set(index: Int, newValue: List): List {
        -- Returning itself to indicate that no change can be made.
        self
    };

    size(): Int {
        0  -- Abstract size method to be overridden by subclasses
    };

    print(): IO {
        out_string("[]\n") -- Empty list representation
    };

    append(list: List): List { list }; -- Append another list to the current one

    reverse(): List { self }; -- Reverse the list (abstract implementation for `Nil`)

    filterBy(filter: Filter): List { 
        self 
    };
    

};

class Cons inherits List {
    hd: Object;  -- Head of the list
    tl: List;    -- Tail of the list (the rest of the elements)

    init(h: Object, t: List): Cons {
       { hd <- h;
        tl <- t;
        self;
       }
    };

    isEmpty(): Bool { false };

    hd(): Object { hd };

    tl(): List { tl };

    size(): Int {
        1 + tl.size()  -- Size is 1 + the size of the tail
    };

    get(index: Int): Object {
        if index = 0 then {
            hd;
        } else {
            tl.get(index - 1);
        } fi
    };

    getList(index: Int): List {
        {
            if index <= 0 then {
                -- If the user provided index is zero or negative, it's invalid
                out_string("Invalid index: ");
                out_int(index);
                out_string(". Returning an empty list.\n");
                new List;
            } else {
                -- Adjust the index for zero-based indexing
                let targetIndex: Int <- index - 1 in {
                    let counter: Int <- 0 in {
                        let current: List <- self in {
                            let result: List <- new List in {  -- Initialize result to an empty list
                                -- Iterate through the list structure to find the required index
                                while not current.isEmpty() loop {
                                    if counter = targetIndex then {
                                        -- Successfully found the list at the given index
                                        result <- current;  -- Store the current list in the result
                                        current <- new List;  -- Break the loop by setting current to empty
                                    } else {
                                        -- Move to the next element in the list
                                        current <- current.tl();
                                        counter <- counter + 1;
                                    } fi;
                                } pool;
    
                                -- Return the result, which will either be the found list or an empty list if not found
                                result;
                            };
                        };
                    };
                };
            }fi;
        }
    };

    flattenList(): List {
        {
            let current: List <- self in {
                let flattenedList: List <- new List in {
                    while not current.isEmpty() loop {
                        let item: Object <- current.hd() in {
                            case item of
                                cons: Cons => {
                                    let nestedList: List <- cons.flattenList() in {
                                        flattenedList <- flattenedList.append(nestedList);
                                    };
                                };
                                other: Object => {
                                    flattenedList <- new Cons.init(item, flattenedList);
                                };
                            esac;
                        };
                        current <- current.tl();
                    } pool;
                    flattenedList.reverse();  -- To maintain original order
                };
            };
        }
    };
    

    sortBy(comp: Comparator, ascending: Bool): List {
        {
            let sortedList: List <- self in {
                let isSorted: Bool <- false in {
                    -- Simple bubble sort-like implementation
                    while not isSorted loop {
                        let swapped: Bool <- false in {
                            let currentNode: List <- sortedList in {
                                while not currentNode.tl().isEmpty() loop {
                                    -- Use case to determine if currentNode is a Cons
                                    case currentNode of
                                        consNode: Cons => {
                                            let nextNode: List <- consNode.tl() in {
                                                case nextNode of
                                                    nextCons: Cons => {
                                                        let item1: Object <- consNode.hd() in {
                                                            let item2: Object <- nextCons.hd() in {
                                                                let comparison: Int <- comp.compareTo(item1, item2) in {
                                                                    -- Perform the swap based on comparison
                                                                    if ascending then {
                                                                        if comparison = 1 then {
                                                                            -- Swap if item1 > item2
                                                                            consNode.setHd(item2);
                                                                            nextCons.setHd(item1);
                                                                            swapped <- true;
                                                                        } else {
                                                                            swapped <- swapped;
                                                                        } fi;
                                                                    } else {
                                                                        if comparison = 0 - 1 then {
                                                                            -- Swap if item1 < item2 for descending order
                                                                            consNode.setHd(item2);
                                                                            nextCons.setHd(item1);
                                                                            swapped <- true;
                                                                        } else {
                                                                            swapped <- swapped;
                                                                        } fi;
                                                                    } fi;
                                                                };
                                                            };
                                                        };
                                                    };
                                                    other: Object => {
                                                        -- Do nothing if nextNode is not a Cons
                                                        swapped <- swapped;
                                                    };
                                                esac;
                                            };
                                        };
                                        other: Object => {
                                            -- Do nothing if currentNode is not a Cons
                                            swapped <- swapped;
                                        };
                                    esac;
                                    currentNode <- currentNode.tl();
                                } pool;
    
                                -- If no elements were swapped, the list is sorted
                                if not swapped then {
                                    isSorted <- true;
                                } else {
                                    isSorted <- false;
                                } fi;
                            };
                        };
                    } pool;
                    sortedList;
                };
            };
        }
    };
    
    -- Set head method for swapping
    setHd(newHd: Object): SELF_TYPE {
      {  hd <- newHd;
        self;
      }
    };
    
    

    set(index: Int, newValue: List): List {
        if index = 0 then {
            -- Replace the current node with the new value
            new Cons.init(newValue, tl);
        } else {
            -- Recursively call `set` on the tail
            new Cons.init(hd, tl.set(index - 1, newValue));
        } fi
    };

    append(list: List): List {
        new Cons.init(hd, tl.append(list))
         -- Recursively add `hd` to the result of appending `list` to the tail
    };

    reverse(): List {
        let reversedTail: List <- tl.reverse()
        in
            reversedTail.append(new List.cons(hd))
    };

    filterBy(filter: Filter): List {
        {
            let filteredList: List <- new List in {
                let current: List <- self in {
                    while not current.isEmpty() loop {
                        -- Ensure that we are correctly accessing each `hd` item
                        let item: Object <- current.hd() in {
                            -- Handle specific types just like in the `printList` function
                            case item of
                                p: Product => {
                                    if filter.apply(p) then {
                                        filteredList <- new Cons.init(p, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                r: Rank => {
                                    if filter.apply(r) then {
                                        filteredList <- new Cons.init(r, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                s: StringObject => {
                                    if filter.apply(s) then {
                                        filteredList <- new Cons.init(s, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                i: IntObject => {
                                    if filter.apply(i) then {
                                        filteredList <- new Cons.init(i, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                b: BoolObject => {
                                    if filter.apply(b) then {
                                        filteredList <- new Cons.init(b, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                io: IOObject => {
                                    if filter.apply(io) then {
                                        filteredList <- new Cons.init(io, filteredList);
                                    } else {
                                        filteredList <- filteredList;
                                    } fi;
                                };
                                cons: Cons => {
                                    -- Treat it as a nested list and recursively filter the elements
                                    let nestedList: List <- cons.filterBy(filter).reverse() in {
                                        filteredList <- filteredList.append(nestedList);
                                    };
                                };
                                other: Object => {
                                    out_string("Checking item: Unknown type\n");
                                };
                            esac;
                        };
                        -- Move to the next node in the list
                        current <- current.tl();
                    } pool;
    
                    -- Return the reversed list to maintain the original order
                    filteredList.reverse();
                };
            };
        }
    };
    
};

class StringList {
    -- Represents an empty list of strings

    isEmpty(): Bool { true };

    hd(): String { {abort(); "";} };  -- No head for an empty list
    tl(): StringList { {abort(); self; } };  -- No tail for an empty list

    size(): Int { 0 };  -- Empty list has size 0

    get(index: Int): String {
        {abort(); "";} -- Cannot get an element from an empty list
    };

    cons(h: String): StringList {
        new StringConsList.init(h, self)  -- Create a new `StringConsList` with the given head and this list as the tail
    };

    append(list: StringList): StringList { list }; -- Append another list to the current one

    reverse(): StringList { self }; -- Reversing an empty list yields itself
};

class StringConsList inherits StringList {
    hd: String;       -- Head of the list (a `String`)
    tl: StringList;   -- Tail of the list (the rest of the elements)

    init(h: String, t: StringList): StringConsList {
        {
            hd <- h;
            tl <- t;
            self;
        }
    };

    isEmpty(): Bool { false };

    hd(): String { hd };  -- Return the head as a `String`

    tl(): StringList { tl };  -- Return the tail as a `StringList`

    size(): Int {
        1 + tl.size()  -- Size is 1 + the size of the tail
    };

    get(index: Int): String {
        if index = 0 then {
            hd;  -- Since `hd` is a `String`, return it directly
        } else {
            tl.get(index - 1);
        } fi
    };

    append(list: StringList): StringList {
        new StringConsList.init(hd, tl.append(list)) -- Recursively add `hd` to the result of appending `list` to the tail
    };
};

-- atoi implementation

class A2I {

    c2i(char : String) : Int {
   if char = "0" then 0 else
   if char = "1" then 1 else
   if char = "2" then 2 else
       if char = "3" then 3 else
       if char = "4" then 4 else
       if char = "5" then 5 else
       if char = "6" then 6 else
       if char = "7" then 7 else
       if char = "8" then 8 else
       if char = "9" then 9 else
       { abort(); 0; }  -- the 0 is needed to satisfy the typchecker
       fi fi fi fi fi fi fi fi fi fi
    };

(*
  i2c is the inverse of c2i.
*)
    i2c(i : Int) : String {
   if i = 0 then "0" else
   if i = 1 then "1" else
   if i = 2 then "2" else
   if i = 3 then "3" else
   if i = 4 then "4" else
   if i = 5 then "5" else
   if i = 6 then "6" else
   if i = 7 then "7" else
   if i = 8 then "8" else
   if i = 9 then "9" else
   { abort(); ""; }  -- the "" is needed to satisfy the typchecker
       fi fi fi fi fi fi fi fi fi fi
    };

(*
  a2i converts an ASCII string into an integer.  The empty string
is converted to 0.  Signed and unsigned strings are handled.  The
method aborts if the string does not represent an integer.  Very
long strings of digits produce strange answers because of arithmetic 
overflow.

*)
    a2i(s : String) : Int {
       if s.length() = 0 then 0 else
   if s.substr(0,1) = "-" then ~a2i_aux(s.substr(1,s.length()-1)) else
       if s.substr(0,1) = "+" then a2i_aux(s.substr(1,s.length()-1)) else
          a2i_aux(s)
       fi fi fi
    };

(*
 a2i_aux converts the usigned portion of the string.  As a programming
example, this method is written iteratively.
*)
    a2i_aux(s : String) : Int {
   (let int : Int <- 0 in	
          {	
              (let j : Int <- s.length() in
             (let i : Int <- 0 in
           while i < j loop
           {
               int <- int * 10 + c2i(s.substr(i,1));
               i <- i + 1;
           }
           pool
         )
          );
             int;
       }
       )
    };

(*
   i2a converts an integer to a string.  Positive and negative 
numbers are handled correctly.  
*)
   i2a(i : Int) : String {
   if i = 0 then "0" else 
       if 0 < i then i2a_aux(i) else
         "-".concat(i2a_aux(i * ~1)) 
       fi fi
   };
   
(*
   i2a_aux is an example using recursion.
*)		
   i2a_aux(i : Int) : String {
       if i = 0 then "" else 
       (let next : Int <- i / 10 in
       i2a_aux(next).concat(i2c(i - next * 10))
       )
       fi
   };

};
