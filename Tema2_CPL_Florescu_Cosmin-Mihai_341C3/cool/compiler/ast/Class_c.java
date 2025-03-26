package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Class_c extends ASTNode {
    private final String name;
    private final String parent;
    private final List<Feature> features;
    private final List<Attribute> attributes = new ArrayList<>();
    private final List<Method> methods = new ArrayList<>();

    private final DefaultScope attributeScope = new DefaultScope(null);
    private final DefaultScope methodeScope = new DefaultScope(null);

    // ANTLR-specific context for error locations
    private final CoolParser.ClassContext ctx;

    public Class_c(CoolParser.ClassContext ctx,
                  Token token,
                  String name,
                  String parent,
                  List<Feature> features) {
        super(token);
        this.ctx = ctx;
        this.name = name;
        this.parent = parent;
        this.features = features;

        // Separate features into attributes and methods
        for (var feat : features) {
            if (feat instanceof Attribute) {
                attributes.add((Attribute) feat);
            } else if (feat instanceof Method) {
                methods.add((Method) feat);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public DefaultScope getAttributeScope() {
        return attributeScope;
    }

    public DefaultScope getMethodeScope() {
        return methodeScope;
    }

    // -------------------------------------------------------------------------
    // 1) checkClassName()
    // -------------------------------------------------------------------------
    /**
     * Validates that the class name is not SELF_TYPE and not redefined.
     */
    public boolean checkClassName() {
        if ("SELF_TYPE".equals(name)) {
            SymbolTable.error(ctx, ctx.name,
                    "Class has illegal name SELF_TYPE");
            return true;
        }
        if (SymbolTable.globals.lookup(name) != null) {
            SymbolTable.error(ctx, ctx.name,
                    String.format("Class %s is redefined", name));
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // 2) checkParent()
    // -------------------------------------------------------------------------
    /**
     * Ensures the parent is neither illegal (e.g., Int, String, SELF_TYPE) nor undefined.
     */
    public void checkParent() {
        if (parent == null) {
            // No explicit parent => no checks needed
            return;
        }
        var illegalSet = Set.of(
                Constants.INT, Constants.STRING, Constants.OBJECT,
                Constants.IO, Constants.SELF_TYPE, Constants.BOOL
        );

        boolean illegalParent = false;
        if (illegalSet.contains(parent)) {
            SymbolTable.error(ctx, ctx.parent,
                    String.format("Class %s has illegal parent %s", name, parent));
            illegalParent = true;
        }
        if (SymbolTable.globals.lookup(parent) == null) {
            SymbolTable.error(ctx, ctx.parent,
                    String.format("Class %s has undefined parent %s", name, parent));
            illegalParent = true;
        }

        if (!illegalParent) {
            setParentScope();
        }
    }

    // -------------------------------------------------------------------------
    // 3) checkCycle()
    // -------------------------------------------------------------------------
    /**
     * Detects inheritance cycles by walking up the ancestry chain
     * to see if this class reappears.
     */
    public void checkCycle() {
        var parentSymbol = (ClassSymbol) SymbolTable.globals.lookup(parent);
        while (parentSymbol != null) {
            var node = parentSymbol.getpClass();
            if (node == this) {
                SymbolTable.error(ctx, ctx.name,
                        String.format("Inheritance cycle for class %s", name));
                break;
            }
            if (node == null) {
                break;
            }
            parentSymbol = (ClassSymbol) SymbolTable.globals.lookup(node.parent);
        }
    }

    // -------------------------------------------------------------------------
    // 4) checkAttributes()
    // -------------------------------------------------------------------------
    /**
     * Check for duplicate attributes, 'self' naming, undefined types,
     * or redefinition of inherited attributes. Valid attributes go into scope.
     */
    public void checkAttributes() {
        var visitedAttrNames = new HashSet<String>();

        for (var attr : attributes) {
            var idName = attr.getId();
            if (!visitedAttrNames.add(idName)) {
                // Already inspected
                continue;
            }
            var duplicates = new ArrayList<Attribute>();
            for (var a2 : attributes) {
                if (a2.getId().equals(idName)) {
                    duplicates.add(a2);
                }
            }
            if (duplicates.size() > 1) {
                for (var d : duplicates.subList(1, duplicates.size())) {
                    SymbolTable.error(d.ctx, d.token,
                            String.format("Class %s redefines attribute %s",
                                    name, idName));
                }
                continue;
            }

            if ("self".equals(idName)) {
                SymbolTable.error(attr.ctx, attr.ctx.start,
                        String.format("Class %s has attribute with illegal name self", name));
                continue;
            }

            if (SymbolTable.globals.lookup(attr.getType()) == null) {
                SymbolTable.error(attr.ctx,
                        ((CoolParser.AttributeContext) attr.ctx).TYPE().getSymbol(),
                        String.format("Class %s has attribute %s with undefined type %s",
                                name, idName, attr.getType()));
                continue;
            }

            if (hasInheritedAttribute(idName)) {
                SymbolTable.error(attr.ctx, attr.token,
                        String.format("Class %s redefines inherited attribute %s", name, idName));
                continue;
            }

            var attrTypeSymbol = (ClassSymbol) SymbolTable.globals.lookup(attr.getType());
            if (attrTypeSymbol != null) {
                attributeScope.add(new Symbol(idName, attrTypeSymbol.getpClass()));
            }
        }
    }

    /**
     * Helper to see if an ancestor class already defines this attribute.
     */
    private boolean hasInheritedAttribute(String attributeName) {
        var symbolParent = (ClassSymbol) SymbolTable.globals.lookup(parent);
        if (symbolParent == null) {
            return false;
        }
        var ancestorNode = symbolParent.getpClass();
        return ancestorNode != null && ancestorNode.hasAttribute(attributeName);
    }

    /**
     * True if this class or any ancestor has an attribute with the given id.
     */
    public boolean hasAttribute(String id) {
        for (var a : attributes) {
            if (a.getId().equals(id)) {
                return true;
            }
        }
        var parentSym = (ClassSymbol) SymbolTable.globals.lookup(parent);
        if (parentSym == null) {
            return false;
        }
        return parentSym.getpClass().hasAttribute(id);
    }

    // -------------------------------------------------------------------------
    // 5) checkAttribute2()
    // -------------------------------------------------------------------------
    /**
     * A second pass to check attribute initializations or other details.
     */
    public void checkAttribute2() {
        for (var attr : attributes) {
            attr.checkAttribute(attributeScope);
        }
    }

    // -------------------------------------------------------------------------
    // 6) checkMethods()
    // -------------------------------------------------------------------------
    /**
     * Checks for method redefinition in the same class, undefined return type,
     * and correct overriding of parent methods.
     */
    public void checkMethods() {
        var visitedMethodNames = new HashSet<String>();
        for (var m : methods) {
            var methodId = m.getId();
            if (!visitedMethodNames.add(methodId)) {
                continue;
            }
            var sameNameMethods = new ArrayList<Method>();
            for (var x : methods) {
                if (methodId.equals(x.getId())) {
                    sameNameMethods.add(x);
                }
            }
            if (sameNameMethods.size() > 1) {
                for (var dupl : sameNameMethods.subList(1, sameNameMethods.size())) {
                    SymbolTable.error(dupl.ctx, dupl.token,
                            String.format("Class %s redefines method %s", name, methodId));
                }
            }

            // Return type must exist
            if (SymbolTable.globals.lookup(m.getType()) == null) {
                SymbolTable.error(m.ctx,
                        ((CoolParser.MethodContext) m.getCtx()).TYPE().getSymbol(),
                        String.format("Class %s has method %s with undefined return type %s",
                                name, methodId, m.getType()));
            }

            // Compare with parent's version
            var inheritedVersion = findParentMethod(methodId);
            if (inheritedVersion != null) {
                if (!inheritedVersion.getType().equals(m.getType())) {
                    SymbolTable.error(m.ctx,
                            ((CoolParser.MethodContext) m.getCtx()).TYPE().getSymbol(),
                            String.format("Class %s overrides method %s but changes return type from %s to %s",
                                    name, methodId, inheritedVersion.getType(), m.getType()));
                }
                if (inheritedVersion.getFormals().size() != m.getFormals().size()) {
                    SymbolTable.error(m.ctx, m.ctx.start,
                            String.format("Class %s overrides method %s with different number of formal parameters",
                                    name, methodId));
                    continue;
                }
                for (int i = 0; i < m.getFormals().size(); i++) {
                    var childF = m.getFormals().get(i);
                    var parentF = inheritedVersion.getFormals().get(i);
                    if (!childF.getType().equals(parentF.getType())) {
                        SymbolTable.error(childF.getCtx(), childF.getCtx().stop,
                                String.format("Class %s overrides method %s but changes type of formal parameter %s from %s to %s",
                                        name, methodId,
                                        childF.getId(), parentF.getType(), childF.getType()));
                    }
                }
            }
        }
    }

    /**
     * Recursively searches up the inheritance chain for a method with 'methodId'.
     */
    private Method findParentMethod(String methodId) {
        var pSym = (ClassSymbol) SymbolTable.globals.lookup(parent);
        if (pSym == null) {
            return null;
        }
        var pNode = pSym.getpClass();
        if (pNode == null) {
            return null;
        }
        for (var mm : pNode.methods) {
            if (methodId.equals(mm.getId())) {
                return mm;
            }
        }
        return pNode.findParentMethod(methodId);
    }

    // -------------------------------------------------------------------------
    // 7) checkParametersMethods()
    // -------------------------------------------------------------------------
    /**
     * Verifies formals: no 'self' param name, no 'SELF_TYPE' param type, must have
     * defined type, and no duplicate formals in the same method.
     */
    public void checkParametersMethods() {
        for (var meth : methods) {
            boolean illegalMeth = false;
            var formalSet = new HashSet<String>();

            for (var f : meth.getFormals()) {
                var fname = f.getId();
                if (!formalSet.add(fname)) {
                    continue;
                }

                if ("self".equals(fname)) {
                    SymbolTable.error(meth.ctx, f.getCtx().ID().getSymbol(),
                            String.format("Method %s of class %s has formal parameter with illegal name self",
                                    meth.getId(), name));
                    illegalMeth = true;
                }
                if ("SELF_TYPE".equals(f.getType())) {
                    SymbolTable.error(meth.ctx, f.getCtx().TYPE().getSymbol(),
                            String.format("Method %s of class %s has formal parameter %s with illegal type SELF_TYPE",
                                    meth.getId(), name, fname));
                    illegalMeth = true;
                }
                if (SymbolTable.globals.lookup(f.getType()) == null) {
                    SymbolTable.error(meth.ctx, f.getCtx().TYPE().getSymbol(),
                            String.format("Method %s of class %s has formal parameter %s with undefined type %s",
                                    meth.getId(), name, fname, f.getType()));
                    illegalMeth = true;
                }

                // Check duplicates in the same method
                var duplicates = new ArrayList<Formal>();
                for (var x : meth.getFormals()) {
                    if (fname.equals(x.getId())) {
                        duplicates.add(x);
                    }
                }
                if (duplicates.size() > 1) {
                    for (var d : duplicates.subList(1, duplicates.size())) {
                        SymbolTable.error(meth.ctx, d.getCtx().ID().getSymbol(),
                                String.format("Method %s of class %s redefines formal parameter %s",
                                        meth.getId(), name, d.getId()));
                        illegalMeth = true;
                    }
                }

                // If no problems, add to the method scope
                if (!illegalMeth) {
                    var typeSymbol = (ClassSymbol) SymbolTable.globals.lookup(meth.getType());
                    if (typeSymbol != null) {
                        methodeScope.add(new Symbol(meth.getId(), typeSymbol.getpClass()));
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 8) checkMethodBody()
    // -------------------------------------------------------------------------
    /**
     * For each method, runs checkMethod(...) with the attribute scope.
     */
    public void checkMethodBody() {
        for (var meth : methods) {
            meth.checkMethod(attributeScope);
        }
    }

    // -------------------------------------------------------------------------
    // setParentScope()
    // -------------------------------------------------------------------------
    /**
     * Links our scopes to the parent's scopes if the parent is valid.
     */
    public void setParentScope() {
        var parentSym = (ClassSymbol) SymbolTable.globals.lookup(parent);
        if (parentSym == null) {
            return;
        }
        var parentCNode = parentSym.getpClass();
        if (parentCNode == null) {
            return;
        }
        attributeScope.setParent(parentCNode.getAttributeScope());
        methodeScope.setParent(parentCNode.getMethodeScope());
    }

    // -------------------------------------------------------------------------
    // Debug/Printing
    // -------------------------------------------------------------------------
    @Override
    protected void printTitle() {
        print("class");
    }

    @Override
    protected void printChildren() {
        print(name);
        if (parent != null) {
            print(parent);
        }
        print(features);
    }

    // -------------------------------------------------------------------------
    // checkIfParent() & findCommonParent()
    // -------------------------------------------------------------------------
    /**
     * Determine if 'parentClass' is on our ancestor chain.
     */
    public boolean checkIfParent(String parentClass) {
        if (name.equals(parentClass)) {
            return true;
        }
        var parentCLS = SymbolTable.globals.lookup(parent);
        while (parentCLS != null) {
            if (parentCLS.getName().equals(parentClass)) {
                return true;
            }
            var nextParent = parentCLS.getTypeClass().getParent();
            parentCLS = SymbolTable.globals.lookup(nextParent);
        }
        return false;
    }

    /**
     * Find the closest common ancestor with 'parentClass'.
     */
    public Class_c findCommonParent(Class_c parentClass) {
        if (name.equals(parentClass.getName())) {
            return this;
        }
        var parentCLS = SymbolTable.globals.lookup(parent);
        while (parentCLS != null) {
            if (checkIfParent(parentCLS.getName())) {
                return parentCLS.getTypeClass();
            }
            parentCLS = SymbolTable.globals.lookup(parentCLS.getTypeClass().getParent());
        }
        return Constants.OBJECT_CLASS_C;
    }
}
