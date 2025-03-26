package cool.structures;

public interface Scope<T extends Symbol> {
    boolean add(T sym);

    T lookup(String str);

    T lookup(String str, boolean recursive);

    Scope<T> getParent();

    void setParent(Scope<T> parent);

    ClassSymbol getCurrentClass();
}
