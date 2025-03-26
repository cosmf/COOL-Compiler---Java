package cool.compiler.ast;

import cool.compiler.ast.expression.MethodCall;
import cool.parser.CoolParser;
import cool.structures.*;

import java.util.*;

public class PClass extends ASTNode {
    private final CoolParser.ClassContext context;
    private final String name;
    private final String parent;
    private final List<Feature> features;
    private final List<Attribute> attributes;
    private final List<Method> methods;

    public PClass(CoolParser.ClassContext context, String name, String parent, List<Feature> features) {
        this.context = context;
        this.name = name;
        this.parent = parent;
        this.features = features;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
        for (Feature f : features) {
            if (f instanceof Attribute a) attributes.add(a);
            else if (f instanceof Method m) methods.add(m);
            else throw new RuntimeException("Unknown feature: " + f);
        }
    }

    @Override
    public CoolParser.ClassContext getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Method> getMethods() {
        return methods;
    }

    @Override
    protected void printTitle() {
        print("class");
    }

    @Override
    protected void printChildren() {
        print(name);
        if (parent != null) print(parent);
        print(features);
    }

    public Map<String, VariableSymbol> getAllAttributes(){
        Map<String, VariableSymbol> output1 = new LinkedHashMap<>();
        Map<String, VariableSymbol> output = new LinkedHashMap<>();
        ClassSymbol cls = SymbolTable.lookupClass(name);
        while(cls != null){
            var allAttributes = ((DefaultScope<VariableSymbol>)cls.getAttributeScope()).getSymbols();
            for(var attribute : allAttributes.keySet()){
                if(attribute.equals("self")) continue;
                output1.put(attribute, allAttributes.get(attribute));
            }
            cls = cls.getParent();
        }
        List<String> keys = new ArrayList<>(output1.keySet());
        Collections.reverse(keys);
        for(var key : keys){
            output.put(key, output1.get(key));
        }
        return output;
    }

    public Map<String, String> getAllMethods(){
        Map<String, String> output1 = new LinkedHashMap<>();
        Map<String, String> output = new LinkedHashMap<>();
        ClassSymbol cls = SymbolTable.lookupClass(name);
        while (cls != null){
            var allMethods = ((DefaultScope<MethodSymbol>)cls.getMethodScope()).getSymbols();
            var keySet = new ArrayList<>(allMethods.keySet());
            Collections.reverse(keySet);
            for(var meth : keySet){
                if(!output1.containsKey(meth))
                    output1.put(meth, cls.getName());
            }
            cls = cls.getParent();
        }
        List<String> keys = new ArrayList<>(output1.keySet());
        Collections.reverse(keys);
        for(var key : keys){
            output.put(key, output1.get(key));
        }
        return output;
    }

    public Map<String, MethodSymbol> getClassMethods(){
        Map<String, MethodSymbol> output = new LinkedHashMap<>();
        ClassSymbol cls = SymbolTable.lookupClass(name);
        while (cls != null){
            var allMethods = ((DefaultScope<MethodSymbol>)cls.getMethodScope()).getSymbols();
            for(var meth : allMethods.keySet()){
                if(meth.equals("main") || meth.equals("copy") || meth.equals("abort") ||
                    meth.equals("type_name") || meth.equals("out_int") || meth.equals("in_int")
                    || meth.equals("out_string") || meth.equals("in_string")) continue;
                output.put(meth, allMethods.get(meth));
            }
            cls = cls.getParent();
        }
        return output;
    }
}
