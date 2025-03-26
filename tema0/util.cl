class Comparator {
    compareTo(o1 : Object, o2 : Object):Int {0};
};

class PriceComparator inherits Comparator {
    compareTo(o1: Object, o2: Object): Int {
        {
            -- Step 1: Check if the first object is a Product
            case o1 of
                p1: Product => {
                    -- Step 2: Check if the second object is a Product
                    case o2 of
                        p2: Product => {
                            -- Get the prices of both products
                            let price1: Int <- p1.getprice() in {
                                let price2: Int <- p2.getprice() in {
                                    -- Compare the prices
                                    if price1 < price2 then {
                                        0-1;
                                    } else if price1 = price2 then {
                                        0;
                                    } else {
                                        1;
                                    } fi fi;
                                };
                            };
                        };
                        other: Object => {
                            -- If o2 is not a Product, call abort
                            abort();
                            0;
                        };
                    esac;
                };
                other: Object => {
                    -- If o1 is not a Product, call abort
                    abort();
                    0;
                };
            esac;
        }
    };
};

class RankComparator inherits Comparator {
    -- Define class attributes for counters
    privateCounter: Int;
    corporalCounter: Int;
    sergentCounter: Int;
    officerCounter: Int;

    init(): SELF_TYPE {{
        privateCounter <- 0;
        corporalCounter <- 0;
        sergentCounter <- 0;
        officerCounter <- 0;
        self;
    }};

    compareTo(o1: Object, o2: Object): Int {
        {
            case o1 of
                r1: Rank => {
                    case o2 of
                        r2: Rank => {
                            let rank1: Int <- r1.getRankLevel() in {
                                let rank2: Int <- r2.getRankLevel() in {
                                    if rank1 < rank2 then {
                                        0 - 1; -- Return -1 if r1 is of lower rank
                                    } else if rank1 = rank2 then {
                                        let order1: Int <- r1.getOrder() in {
                                            let order2: Int <- r2.getOrder() in {
                                                if order1 < order2 then {
                                                    0 - 1; -- Return -1 if r1 appears before r2
                                                } else if order1 = order2 then {
                                                    0; -- Return 0 if they have the same order
                                                } else {
                                                    1; -- Return 1 if r1 appears after r2
                                                } fi fi;
                                            };
                                        };
                                    } else {
                                        1; -- Return 1 if r1 is of higher rank
                                    } fi fi;
                                };
                            };
                        };
                        other: Object => {
                            abort();
                            0;
                        };
                    esac;
                };
                other: Object => {
                    abort();
                    0;
                };
            esac;
        }
    };

    

    -- Helper method to assign unique values to ranks
    assignRankValue(rank: Rank): Int {
        {
            case rank of
                p: Private => {
                    privateCounter <- privateCounter + 1;
                    p.getRankLevel() + privateCounter;
                };
                c: Corporal => {
                    corporalCounter <- corporalCounter + 1;
                    c.getRankLevel() + corporalCounter;
                };
                s: Sergent => {
                    sergentCounter <- sergentCounter + 1;
                    s.getRankLevel() + sergentCounter;
                };
                o: Officer => {
                    officerCounter <- officerCounter + 1;
                    o.getRankLevel() + officerCounter;
                };
                other: Object => {
                    0;  -- Default value for unknown type, should never be used
                };
            esac;
        }
    };
};

class AlphabeticComparator inherits Comparator {
    compareTo(o1: Object, o2: Object): Int {
        {
            -- Step 1: Check if the first object is a StringObject
            case o1 of
                s1: StringObject => {
                    -- Step 2: Check if the second object is also a StringObject
                    case o2 of
                        s2: StringObject => {
                            let value1: String <- s1.getValue() in {
                                let value2: String <- s2.getValue() in {
                                    if value1 < value2 then {
                                        0 - 1; -- Return -1 if value1 is lexicographically less than value2
                                    } else if value1 = value2 then {
                                        0; -- Return 0 if they are equal
                                    } else {
                                        1; -- Return 1 if value1 is lexicographically greater than value2
                                    } fi fi;
                                };
                            };
                        };
                        other: Object => {
                            -- If o2 is not a StringObject, call abort
                            abort();
                            0;
                        };
                    esac;
                };
                other: Object => {
                    -- If o1 is not a StringObject, call abort
                    abort();
                    0;
                };
            esac;
        }
    };
};

    



class Filter{
    apply(item: Object): Bool {
        { abort(); false; } -- Default implementation returns false, to be overridden
    };
};

class ProductFilter inherits Filter {
    apply(item: Object): Bool {
        {
            case item of
                p: Product => {
                    true;
                };
                other : Object => {
                    false;
                };
            esac;
        }
    };
};

class RankFilter inherits Filter {
    apply(item: Object): Bool {
        {
            case item of
                r: Rank => {
                    true;
                };
                other: Object => {
                    false;
                };
            esac;
        }
    };
};

class SamePriceFilter inherits Filter {
    apply(item: Object): Bool {
        {
            case item of
                p: Product => {
                    -- Get the generic price from the Product class
                    let genericPrice: Int <- p.calcGenericPrice() in {
                        case p of
                            e: Edible => {
                                case e of
                                    s: Soda => {
                                        let specificPrice: Int <- s.getprice() in {
                                            if specificPrice = genericPrice then {
                                                true;
                                            } else {
                                                false;
                                            } fi;
                                        };
                                    };
                                    c: Coffee => {
                                        let specificPrice: Int <- c.getprice() in {
                                            if specificPrice = genericPrice then {
                                                true;
                                            } else {
                                                false;
                                            } fi;
                                        };
                                    };
                                    other: Object => {
                                        false;
                                    };
                                esac;
                            };
                            l: Laptop => {
                                let specificPrice: Int <- l.getprice() in {
                                    if specificPrice = genericPrice then {
                                        true;
                                    } else {
                                        false;
                                    } fi;
                                };
                            };
                            r: Router => {
                                let specificPrice: Int <- r.getprice() in {
                                    if specificPrice = genericPrice then {
                                        true;
                                    } else {
                                        false;
                                    } fi;
                                };
                            };
                            other: Object => {
                                false;
                            };
                        esac;
                    };
                };
                other: Object => {
                    false;
                };
            esac;
        }
    };
};
