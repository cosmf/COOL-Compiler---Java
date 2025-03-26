package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultScope<T extends Symbol> implements Scope<T> {

    private final Map<String, T> symbols = new LinkedHashMap<>();
    private Scope<T> parent;
    private ClassSymbol currentClass;

    public DefaultScope() {
    }

    public DefaultScope(Scope<T> parent) {
        setParent(parent);
    }

    public DefaultScope(ClassSymbol currentClass) {
        this.currentClass = currentClass;
    }

    public Map<String, T> getSymbols() {
        return symbols;
    }

    @Override
    public boolean add(T sym) {
        // Reject duplicates in the same scope.
        if (symbols.containsKey(sym.getName()))
            return false;

        symbols.put(sym.getName(), sym);

        return true;
    }

    @Override
    public T lookup(String name) {
        return lookup(name, true);
    }

    @Override
    public T lookup(String name, boolean recursive) {
        var sym = symbols.get(name);

        if (sym != null)
            return sym;

        if (recursive && parent != null)
            return parent.lookup(name);

        return null;
    }

    @Override
    public Scope<T> getParent() {
        return parent;
    }

    public void setParent(Scope<T> parent) {
        this.parent = parent;
        if (this.currentClass == null) this.currentClass = parent != null ? parent.getCurrentClass() : null;
    }

    @Override
    public ClassSymbol getCurrentClass() {
        return currentClass;
    }

    @Override
    public String toString() {
        return symbols.values().toString();
    }
}
