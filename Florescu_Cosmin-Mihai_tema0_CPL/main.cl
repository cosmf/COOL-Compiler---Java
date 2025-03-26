class Main inherits IO{

    lists: List <- new List; -- List of lists
    looping: Bool <- true;
    somestr: String;
    someint: Int;

    main():Object {
        {

        someint <- 0;
        self.load();
        while (looping) loop {
            -- out_string("Enter a command (help, load, print, quit): ");
            somestr <- in_string();
            if somestr.length() = 0 then {
                -- If the user presses enter without typing anything
                out_string("Please enter a valid command.\n");
            } else {
                if somestr = "help" then {
                    help();
                } else if somestr = "load" then {
                    -- Call the `load` function
                    self.load();
                } else if somestr = "quit" then {
                    looping <- false;
                    out_string("Goodbye!\n");
                } else if 4 < somestr.length() then {
                        -- Check if the input is "print"
                    if somestr.substr(0, 5) = "print" then {
                        
                        -- Check if the input is exactly "print"
                        if somestr.length() = 5 then {
                            -- Print all lists if the input is just "print"
                            self.printAllLists();
                        } else if somestr.length() < 6 then {
                            out_string("Invalid print command.\n");
                        } else {
                            -- Extract the index if the input is "print <index>"
                            let indexStr: String <- somestr.substr(6, somestr.length() - 6) in {
                                self.printSingleList(indexStr);
                            };
                        } fi fi;
                    } else if somestr.substr(0, 5) = "merge" then {
                        let indices: String <- somestr.substr(6, somestr.length() - 6) in {
                            let tokenizer: StringTokenizer <- new StringTokenizer.init(indices) in {
                                if tokenizer.hasMoreTokens() then {
                                    let index1Str: String <- tokenizer.nextToken() in {
                                        if tokenizer.hasMoreTokens() then {
                                            let index2Str: String <- tokenizer.nextToken() in {
                                                self.mergeLists(index1Str, index2Str);
                                            };
                                        } else {
                                            out_string("Missing second index for merge.\n");
                                        } fi;
                                    };
                                } else {
                                    out_string("Missing indices for merge.\n");
                                } fi;
                            };
                        };
                    } else if somestr.substr(0, 8) = "filterBy" then {
                        let remainingStr: String <- somestr.substr(8, somestr.length() - 8) in {
                            -- Split the remainingStr into index and filter type
                            let tokenizer: StringTokenizer <- new StringTokenizer.init(remainingStr) in {
                                if tokenizer.hasMoreTokens() then {
                                    let indexStr: String <- tokenizer.nextToken() in {
                                        if tokenizer.hasMoreTokens() then {
                                            let filterType: String <- tokenizer.nextToken() in {
                                                -- Call the appropriate filter method based on the filterType
                                                let filterInstance: Filter <- new Filter in {
                                                    if filterType = "ProductFilter" then {
                                                        filterInstance <- new ProductFilter;
                                                    } else if filterType = "RankFilter" then {
                                                        filterInstance <- new RankFilter;
                                                    } else if filterType = "SamePriceFilter" then {
                                                        filterInstance <- new SamePriceFilter;
                                                    } else {
                                                        out_string("Unknown filter type.\n");
                                                    } fi fi fi;
                                                    -- Apply the filter if a valid filter was provided
                                                    if not (filterInstance = new Filter) then {
                                                        let listIndex: Int <- (new A2I).a2i(indexStr) in {
                                                            -- Validate the index
                                                            if listIndex <= 0 then {
                                                                out_string("Invalid index for filtering.\n");
                                                            } else {
                                                                let targetList: List <- lists.getList(listIndex) in {
                                                                    if targetList.isEmpty() then {
                                                                        out_string("Invalid list index.\n");
                                                                    } else {
                                                                        let filteredList: List <- targetList.filterBy(filterInstance) in {
                                                                            -- Update the list at the same index with the filtered list
                                                                            lists <- lists.set(listIndex - 1, filteredList);
                                                                        };
                                                                    } fi;

                                                                };
                                                            } fi;
                                                        };
                                                    } else {
                                                        out_string("No filter type provided.\n");
                                                    } fi;
                                                };
                                            };
                                        } else {
                                            out_string("No filter type provided.\n");
                                        } fi;
                                    };
                                } else {
                                    out_string("No index provided.\n");
                                } fi;
                            };
                        };
                    } else if somestr.substr(0, 6) = "sortBy" then {
                        let remainingStr: String <- somestr.substr(6, somestr.length() - 6) in {
                            let tokenizer: StringTokenizer <- new StringTokenizer.init(remainingStr) in {
                                if tokenizer.hasMoreTokens() then {
                                    let indexStr: String <- tokenizer.nextToken() in {
                                        if tokenizer.hasMoreTokens() then {
                                            let comparatorStr: String <- tokenizer.nextToken() in {
                                                if tokenizer.hasMoreTokens() then {
                                                    let orderStr: String <- tokenizer.nextToken() in {
                                                        -- Convert the indexStr to an integer
                                                        let a2iInstance: A2I <- new A2I in {
                                                            let index: Int <- a2iInstance.a2i(indexStr) in {
                                                                -- Get the list at the given index
                                                                let targetList: List <- lists.getList(index) in {
                                                                    if targetList.isEmpty() then {
                                                                        out_string("Invalid list index.\n");
                                                                    } else {
                                                                        -- Flatten the list first
                                                                        let flatList: List <- targetList.flattenList() in {
                                                                            -- Determine the comparator instance
                                                                            let comparator: Comparator <- new Comparator in {
                                                                                if comparatorStr = "PriceComparator" then {
                                                                                    comparator <- new PriceComparator;
                                                                                } else if comparatorStr = "RankComparator" then {
                                                                                    comparator <- new RankComparator;
                                                                                } else if comparatorStr = "AlphabeticComparator" then {
                                                                                    comparator <- new AlphabeticComparator;
                                                                                } else {
                                                                                    out_string("Unknown comparator type.\n");
                                                                                    comparator <- new Comparator;  -- Use default comparator
                                                                                } fi fi fi;
                                                                                -- Determine the order type (ascending/descending)
                                                                                let ascending: Bool <- true in {
                                                                                    if orderStr = "ascendent" then {
                                                                                        ascending <- true;
                                                                                    } else if orderStr = "descendent" then {
                                                                                        ascending <- false;
                                                                                    } else {
                                                                                        out_string("Unknown order type. Defaulting to ascending.\n");
                                                                                    } fi fi;
                                                                                    -- Sort the list using the comparator and order type
                                                                                    -- self.printListElements(flatList);
                                                                                    case flatList of
                                                                                        consNode: Cons => {
                                                                                            let sortedList: List <- consNode.sortBy(comparator, ascending) in {
                                                                                                -- Update the original list with the sorted list
                                                                                                lists <- lists.set(index - 1, sortedList);
                                                                                            };
                                                                                        };
                                                                                        other: Object => {
                                                                                            out_string("Cannot sort an empty list or unknown type.\n");
                                                                                        };
                                                                                    esac;
                                                                                };
                                                                            };
                                                                        };
                                                                    } fi;
                                                                };
                                                            };
                                                        };
                                                    };
                                                } else {
                                                    out_string("No order type provided.\n");
                                                } fi;
                                            };
                                        } else {
                                            out_string("No comparator type provided.\n");
                                        } fi;
                                    };
                                } else {
                                    out_string("No index provided.\n");
                                } fi;
                            };
                        };
                    }
                     else {
                    out_string("Unknown command.\n");
                } fi fi fi fi;
            } else {
                out_string("Unknown command.\n");
            } fi fi fi fi;
        }fi;
        } pool;

    }
  };

    

  mergeLists(index1Str: String, index2Str: String): Object {
    {
        let a2iInstance: A2I <- new A2I in {
            let index1: Int <- a2iInstance.a2i(index1Str) in {
                let index2: Int <- a2iInstance.a2i(index2Str) in {
                    -- Check for invalid indices
                    if index1 <= 0 then {
                        out_string("Invalid indices for merging.\n");
                    } else if index2 <= 0 then {
                        out_string("Invalid indices for merging.\n");
                    } else if index1 = index2 then {
                        out_string("Invalid indices for merging.\n");
                    } else {
                        let currentLists: List <- lists in {
                            let counter: Int <- 1 in {
                                let list1: List <- new List in {
                                    let list2: List <- new List in {
                                        let remainingLists: List <- new List in {
                                            -- Traverse the list to locate index1 and index2
                                            while not currentLists.isEmpty() loop {
                                                let currentList: Object <- currentLists.hd() in {
                                                    case currentList of
                                                        l: List => {
                                                            if counter = index1 then {
                                                                list1 <- l;
                                                            } else if counter = index2 then {
                                                                list2 <- l;
                                                            } else {
                                                                remainingLists <- new Cons.init(l, remainingLists);
                                                            } fi fi;
                                                        };
                                                    esac;
                                                };
                                                currentLists <- currentLists.tl();
                                                counter <- counter + 1;
                                            } pool;

                                            -- Create a copy of list1 and add elements of list2 to it, preserving their order
                                            let mergedList: List <- new List in {
                                                let tempList1: List <- list1 in {
                                                    -- Copy list1 to mergedList
                                                    while not tempList1.isEmpty() loop {
                                                        mergedList <- new Cons.init(tempList1.hd(), mergedList);
                                                        tempList1 <- tempList1.tl();
                                                    } pool;
                                                    
                                                    -- Append elements from list2 to mergedList
                                                    let tempList2: List <- list2 in {
                                                        while not tempList2.isEmpty() loop {
                                                            mergedList <- new Cons.init(tempList2.hd(), mergedList);
                                                            tempList2 <- tempList2.tl();
                                                        } pool;

                                                        mergedList <- mergedList.reverse();

                                                        -- Traverse to the end of remainingLists to append the merged list at the end
                                                        let current: List <- remainingLists.reverse() in {
                                                            let tempLists: List <- new List in {
                                                                while not current.isEmpty() loop {
                                                                    tempLists <- new Cons.init(current.hd(), tempLists);
                                                                    current <- current.tl();
                                                                } pool;

                                                                -- Add the merged list at the end
                                                                tempLists <- new Cons.init(mergedList, tempLists);

                                                                -- Reverse tempLists to maintain original order
                                                                lists <- tempLists.reverse();
                                                            };
                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };
                            };
                        };
                    } fi fi fi;
                };
            };
        };
        self;
    }
};

    help():Object {
        {
        out_string("Available commands:\n");
        out_string("help - Display this help message\n");
        out_string("load - Load a new list from input\n");
        out_string("quit - Exit the program\n");
        self;
        }
    };

    printListElements(list: List): Object {
        {
            out_string("[ ");
            
            let first: Bool <- true in {
                while not list.isEmpty() loop {
                    let item: Object <- list.hd() in {
                        -- Handle specific types
                        case item of
                            p: Product => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(p.toString());
                            };
                            r: Rank => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(r.toString());
                            };
                            s: StringObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(s.toString());
                            };
                            i: IntObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(i.toString());
                            };
                            b: BoolObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(b.toString());
                            };
                            io: IOObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(io.toString());
                            };
                            c: Cons => {
                                let nestedList: List <- c in {
                                    self.printList(nestedList, 1);
                                };
                            };
                        esac;
                    };
                    list <- list.tl();
                } pool;
            };
    
            out_string(" ]\n");
            self;
        }
    };
    
    -- Function to print all lists
    printAllLists(): Object {
        {
            let counter: Int <- 1 in {
                let currentLists: List <- lists in {
                    while not currentLists.isEmpty() loop {
                        let currentList: Object <- currentLists.hd() in {
                            case currentList of
                                l: List => {
                                    out_int(counter);
                                    out_string(": ");
                                    self.printListElements(l);
                                    -- out_string("\n");
                                    counter <- counter + 1;
                                };
                            esac;
                        };
                        currentLists <- currentLists.tl();
                    } pool;
                };
            };
            self;
        }
    };
    
    -- Print a specific list by index without counter
    printSingleList(option: String): Object {
        {
            let a2iInstance: A2I <- new A2I in {
                let index: Int <- a2iInstance.a2i(option) in {
                    let counter: Int <- 1 in {
                        let currentLists: List <- lists in {
                            -- Traverse to the desired index
                            while not currentLists.isEmpty() loop {
                                if counter = index then {
                                    let currentList: Object <- currentLists.hd() in {
                                        case currentList of
                                            l: List => {
                                                self.printListElements(l);
                                                -- out_string("\n");
                                            };
                                            c: Cons => {
                                                let nestedList: List <- c in {
                                                    self.printListElements(nestedList);
                                                    -- out_string("\n");
                                                };
                                            };
                                        esac;
                                    };
                                    -- Break the loop once the desired list is printed
                                    currentLists <- new List;
                                } else {
                                    currentLists <- currentLists.tl();
                                    counter <- counter + 1;
                                } fi;
                            } pool;
                        };
                    };
                };
            };
            self;
        }
    };
    

    printList(list: List, index: Int): Object {
        {
            out_int(index);
            out_string(": [ ");
    
            let first: Bool <- true in {
                while not list.isEmpty() loop {
                    let item: Object <- list.hd() in {
                        -- Handle specific types
                        case item of
                            p: Product => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(p.toString());
                            };
                            r: Rank => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(r.toString());
                            };
                            s: StringObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(s.toString());
                            };
                            i: IntObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(i.toString());
                            };
                            b: BoolObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(b.toString());
                            };
                            io: IOObject => {
                                if not first then {
                                    out_string(", ");
                                } else {
                                    first <- false;
                                } fi;
                                out_string(io.toString());
                            };
                            c: Cons => {
                                let nestedList: List <- c in {
                                    self.printList(nestedList, index);
                                };
                            };
                            
                        esac;
                    };
                    list <- list.tl();
                } pool;
            };
    
            out_string(" ]\n");
            self;
        }
    };
    

    
    
    

    load(): Object {
        {   let orderCounter: Int <- 1 in {
            -- out_string("Enter objects (type END to finish):\n");
            let local_List: List <- new List in
            let local_loop: Bool <- true in
            while local_loop loop
                let line: String <- in_string() in
                if line = "END" then {
                    local_loop <- false;
                    -- out_string("List loaded.\n");
                    -- Reverse the local list before storing
                    local_List <- local_List.reverse();
                    -- Traverse to the end of the current `lists`
                    let current: List <- lists in {
                        let tempLists: List <- new List in {
                            -- Traverse through the list to the end while constructing a copy
                            while not current.isEmpty() loop {
                                tempLists <- new Cons.init(current.hd(), tempLists);
                                current <- current.tl();
                            } pool;
                    
                            -- Add the new list at the end
                            tempLists <- new Cons.init(local_List, tempLists);
                    
                            -- Reverse tempLists to maintain original order
                            lists <- tempLists.reverse();
                        };
                    };
                } else {
                    let tokenizer: StringTokenizer <- new StringTokenizer.init(line) in
                    if tokenizer.hasMoreTokens() then {
                        let firstToken: String <- tokenizer.nextToken() in
                        {
                            if firstToken = "Laptop" then {
                                let brand: String <- tokenizer.nextToken() in
                                let model: String <- tokenizer.nextToken() in
                                let priceStr: String <- tokenizer.nextToken() in
                                let price: Int <- (new A2I).a2i(priceStr) in
                                let type: String <- "Product" in
                                {
                                    let product: Product <- new Laptop.init(brand, model, price, type) in
                                    {
                                        local_List <- new Cons.init(product, local_List);
                                        -- out_string("Added laptop: ".concat(product.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Router" then {
                                let brand: String <- tokenizer.nextToken() in
                                let model: String <- tokenizer.nextToken() in
                                let priceStr: String <- tokenizer.nextToken() in
                                let price: Int <- (new A2I).a2i(priceStr) in
                                let type: String <- "Product" in
                                {
                                    let product: Product <- new Router.init(brand, model, price, type) in
                                    {
                                        local_List <- new Cons.init(product, local_List);
                                        -- out_string("Added router: ".concat(product.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Coffee" then {
                                let brand: String <- tokenizer.nextToken() in
                                let size: String <- tokenizer.nextToken() in
                                let priceStr: String <- tokenizer.nextToken() in
                                let price: Int <- (new A2I).a2i(priceStr) in
                                let type: String <- "Product" in
                                {
                                    let product: Product <- new Coffee.init(brand, size, price, type) in
                                    {
                                        local_List <- new Cons.init(product, local_List);
                                        -- out_string("Added coffee: ".concat(product.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Soda" then {
                                let brand: String <- tokenizer.nextToken() in
                                let size: String <- tokenizer.nextToken() in
                                let priceStr: String <- tokenizer.nextToken() in
                                let price: Int <- (new A2I).a2i(priceStr) in
                                let type: String <- "Product" in
                                {
                                    let product: Product <- new Soda.init(brand, size, price, type) in
                                    {
                                        local_List <- new Cons.init(product, local_List);
                                        -- out_string("Added soda: ".concat(product.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Private" then {
                                let name: String <- tokenizer.nextToken() in
                                let type: String <- "Rank" in
                                {
                                    let rank: Rank <- new Private.init(name, type, orderCounter) in
                                    {   
                                        orderCounter <- orderCounter + 1;
                                        local_List <- new Cons.init(rank, local_List);
                                        -- out_string("Added private: ".concat(rank.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Corporal" then {
                                let name: String <- tokenizer.nextToken() in
                                let type: String <- "Rank" in
                                {
                                    let rank: Rank <- new Corporal.init(name, type, orderCounter) in
                                    {   
                                        orderCounter <- orderCounter + 1;
                                        local_List <- new Cons.init(rank, local_List);
                                        -- out_string("Added corporal: ".concat(rank.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Sergent" then {
                                let name: String <- tokenizer.nextToken() in
                                let type: String <- "Rank" in
                                {
                                    let rank: Rank <- new Sergent.init(name, type, orderCounter) in
                                    {   
                                        orderCounter <- orderCounter + 1;
                                        local_List <- new Cons.init(rank, local_List);
                                        -- out_string("Added sergent: ".concat(rank.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Officer" then {
                                let name: String <- tokenizer.nextToken() in
                                let type: String <- "Rank" in
                                {
                                    let rank: Rank <- new Officer.init(name, type, orderCounter) in
                                    {   
                                        orderCounter <- orderCounter + 1;
                                        local_List <- new Cons.init(rank, local_List);
                                        -- out_string("Added officer: ".concat(rank.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "String" then {
                                let value: String <- tokenizer.nextToken() in {
                                    let stringObject: StringObject <- new StringObject.init(value) in {
                                        local_List <- new Cons.init(stringObject, local_List);
                                        -- out_string("Added string: ".concat(stringObject.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Int" then {
                                let value: String <- tokenizer.nextToken() in {
                                    let intObject: IntObject <- new IntObject.init(value) in {
                                        local_List <- new Cons.init(intObject, local_List);
                                        -- out_string("Added int: ".concat(intObject.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "Bool" then {
                                let value: String <- tokenizer.nextToken() in {
                                    let boolObject: BoolObject <- new BoolObject.init(value) in {
                                        local_List <- new Cons.init(boolObject, local_List);
                                        -- out_string("Added bool: ".concat(boolObject.toString()).concat("\n"));
                                    };
                                };
                            } else if firstToken = "IO" then {
                                let ioObject: IOObject <- new IOObject in {
                                    local_List <- new Cons.init(ioObject, local_List);
                                    -- out_string("Added IO object: ".concat(ioObject.toString()).concat("\n"));
                                };
                            } else 
                            {
                                out_string("Unknown object type.\n");
                            } fi fi fi fi fi fi fi fi fi fi fi fi;
                        };
                    } else {
                        out_string("No tokens found.\n");
                    } fi;
                } fi
            pool;
        };
        }
    };
};

class StringTokenizer {
    str: String;  -- The string to be tokenized
    tokens: StringList; -- List of tokens
    position: Int <- 0; -- Current position in the list

    init(s: String): StringTokenizer {
        {
            str <- s;
            tokens <- new StringList;
            self.tokenize();
            position <- 0;
            self;
        }
    };

    -- Tokenize the string and store the tokens in the list
    tokenize(): Object {
        
            let currentToken: String <- "" in
            {
                -- Iterate over each character in the string
                position <- 0;
                while position < str.length() loop
                    let currentChar: String <- str.substr(position, 1) in
                    {
                        if currentChar = " " then {
                            if not (currentToken.length() = 0) then {
                                tokens <- tokens.append(new StringConsList.init(currentToken, new StringList));
                                currentToken <- "";
                            } else {
                                currentToken <- currentToken;
                            } fi;
                        } else {
                            currentToken <- currentToken.concat(currentChar);
                        } fi;
                        position <- position + 1;
                    }
                pool;

                -- Add the last token if there's any
                if not (currentToken.length() = 0) then {
                    tokens <- tokens.append(new StringConsList.init(currentToken, new StringList));
                } else {
                    -- If currentToken length is 0, do nothing
                    currentToken <- currentToken;
                } fi;
            }
        
    };
    -- Check if there are more tokens
    hasMoreTokens(): Bool {
        -- not (position <= tokens.size())
        position < tokens.size()
    };

    -- Get the next token
    nextToken(): String {
        let result: String <- tokens.get(position) in
        {
            position <- position + 1;
            result;
        }
    };
};