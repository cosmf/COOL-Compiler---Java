package cool.compiler.ast;

import cool.compiler.Constants;
import cool.structures.Scope;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Case extends Expression {
    private final Expression target;
    private final List<CaseBranch> branches;

    public Case(Token token, Expression target, List<CaseBranch> branches) {
        super(token);
        this.target = target;
        this.branches = branches;
    }

    @Override
    protected void printTitle() {
        print("case");
    }

    @Override
    protected void printChildren() {
        print(target);
        print(branches);
    }


    @Override
    public Class_c checkExpression(Scope scope) {
        // Evaluate the target expression and catch any errors
        evaluateTargetExpression(scope);

        // Check the validity of all case branches
        if (!validateBranches(scope)) {
            return getDefaultType();
        }

        // Calculate and return the unified result type
        return calculateResultType(scope);
    }

    // Helper function to evaluate the target expression
    private void evaluateTargetExpression(Scope scope) {
        target.checkExpression(scope);
    }

    // Helper function to validate all case branches
    private boolean validateBranches(Scope scope) {
        boolean isValid = true;
        for (CaseBranch branch : branches) {
            if (!validateBranchName(branch)) {
                isValid = false;
            }
            if (!validateBranchType(branch)) {
                isValid = false;
            }
            if (!validateBranchDefinition(branch)) {
                isValid = false;
            }
        }
        return isValid;
    }

    // Helper function to validate the name of a case branch
    private boolean validateBranchName(CaseBranch branch) {
        if ("self".equals(branch.getId())) {
            SymbolTable.error(
                branch.getCtx(),
                branch.getCtx().ID().getSymbol(),
                "Case variable has illegal name self"
            );
            return false;
        }
        return true;
    }

    // Helper function to validate the type of a case branch
    private boolean validateBranchType(CaseBranch branch) {
        if ("SELF_TYPE".equals(branch.getType())) {
            SymbolTable.error(
                branch.getCtx(),
                branch.getCtx().TYPE().getSymbol(),
                String.format("Case variable %s has illegal type SELF_TYPE", branch.getId())
            );
            return false;
        }
        return true;
    }

    // Helper function to check if the type of a branch is defined
    private boolean validateBranchDefinition(CaseBranch branch) {
        if (SymbolTable.globals.lookup(branch.getType()) == null) {
            SymbolTable.error(
                branch.getCtx(),
                branch.getCtx().TYPE().getSymbol(),
                String.format("Case variable %s has undefined type %s", branch.getId(), branch.getType())
            );
            return false;
        }
        return true;
    }

    private Class_c getDefaultType() {
        return Constants.OBJECT_CLASS_C;
    }

    private Class_c calculateResultType(Scope scope) {
        Class_c resultType = branches.get(0).checkCaseBranch(scope);
        for (CaseBranch branch : branches.subList(1, branches.size())) {
            Class_c branchType = branch.checkCaseBranch(scope);
            resultType = resultType.findCommonParent(branchType);
        }
        return resultType;
    }
}
