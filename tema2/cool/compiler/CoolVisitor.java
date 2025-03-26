package cool.compiler;

import cool.compiler.ast.*;
import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CoolVisitor extends CoolParserBaseVisitor<ASTNode> {
    private <T extends ParserRuleContext, R extends ASTNode> List<R> visitAll(List<T> list, Function<T, R> visitor) {
        return list.stream().map(visitor).collect(Collectors.toList());
    }

    private String text(Token token) {
        return token == null ? null : token.getText();
    }

    private Expression visitExpr(CoolParser.ExprContext ctx) {
        return ctx == null ? null : (Expression) visit(ctx);
    }

    @Override
    public ASTNode visitExpression(CoolParser.ExpressionContext ctx) {
        return visitExpr(ctx.expr());
    }

    // Program structure: classes, methods, attributes

    @Override
    public Program visitProgram(CoolParser.ProgramContext ctx) {
        return new Program(ctx.start, visitAll(ctx.class_(), this::visitClass));
    }

    @Override
    public Class_c visitClass(CoolParser.ClassContext ctx) {
        // Gather all features into a single list using a helper method
        var features = collectClassFeatures(ctx.feature());

        // Then construct the Class_c node with these features
        return new Class_c(
                ctx,
                ctx.start,
                ctx.name.getText(),
                text(ctx.parent),
                features
        );
    }

    /**
     * Converts the list of feature contexts into a list of Feature AST nodes,
     * checking if each is an Attribute or Method.
     */
    private List<Feature> collectClassFeatures(List<CoolParser.FeatureContext> featureContexts) {
        return featureContexts.stream()
                .map(this::visitSingleFeature) // for each feature, call the helper
                .collect(Collectors.toList());
    }

    /**
     * Visits a single feature context, dispatching to visitAttribute or visitMethod.
     */
    private Feature visitSingleFeature(CoolParser.FeatureContext fctx) {
        if (fctx instanceof CoolParser.AttributeContext) {
            return visitAttribute((CoolParser.AttributeContext) fctx);
        } else if (fctx instanceof CoolParser.MethodContext) {
            return visitMethod((CoolParser.MethodContext) fctx);
        } else {
            // If it's neither, we throw as before
            throw new RuntimeException("Unknown Feature context type: " + fctx);
        }
    }

    @Override
    public Attribute visitAttribute(CoolParser.AttributeContext ctx) {
        return new Attribute(ctx.start, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()), ctx);
    }

    @Override
    public Method visitMethod(CoolParser.MethodContext ctx) {
        return new Method(ctx.start, ctx.ID().getText(), ctx.TYPE().getText(), visitAll(ctx.args, this::visitFormal), visitExpr(ctx.expr()), ctx);
    }

    @Override
    public Formal visitFormal(CoolParser.FormalContext ctx) {
        return new Formal(ctx.start, ctx.ID().getText(), ctx.TYPE().getText(), ctx);
    }

    // Complex expressions

    @Override
    public If visitIf(CoolParser.IfContext ctx) {
        return new If(ctx.start, visitExpr(ctx.cond), visitExpr(ctx.thenBranch), visitExpr(ctx.elseBranch), ctx);
    }

    @Override
    public While visitWhile(CoolParser.WhileContext ctx) {
        return new While(ctx.start, visitExpr(ctx.cond), visitExpr(ctx.body), ctx);
    }

    @Override
    public Let visitLet(CoolParser.LetContext ctx) {
        return new Let(ctx.start, visitAll(ctx.vars, this::visitLocal), visitExpr(ctx.body));
    }

    @Override
    public LetLocal visitLocal(CoolParser.LocalContext ctx) {
        return new LetLocal(ctx.start, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()), ctx);
    }

    @Override
    public Case visitCase(CoolParser.CaseContext ctx) {
        return new Case(ctx.start, visitExpr(ctx.expr()), visitAll(ctx.case_branch(), this::visitCase_branch));
    }

    @Override
    public CaseBranch visitCase_branch(CoolParser.Case_branchContext ctx) {
        return new CaseBranch(ctx.start, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()), ctx);
    }

    // Arithmetic & comparison

    @Override
    public Arithmetic visitArithmetic(CoolParser.ArithmeticContext ctx) {
        return new Arithmetic(ctx.start, Arithmetic.Operation.findBySymbol(ctx.op.getText()), visitExpr(ctx.left), visitExpr(ctx.right), ctx);
    }

    @Override
    public Unary visitUnary(CoolParser.UnaryContext ctx) {
        return new Unary(ctx.start, Unary.Operation.findBySymbol(ctx.op.getText()), visitExpr(ctx.expr()), ctx);
    }

    @Override
    public Comparison visitComparison(CoolParser.ComparisonContext ctx) {
        return new Comparison(ctx.start, Comparison.Operation.findBySymbol(ctx.op.getText()), visitExpr(ctx.left), visitExpr(ctx.right), ctx);
    }

    // Variable stuff

    @Override
    public Variable visitVar(CoolParser.VarContext ctx) {
        return new Variable(ctx.start, ctx.ID().getText(), ctx);
    }

    @Override
    public Assign visitVarAssign(CoolParser.VarAssignContext ctx) {
        return new Assign(ctx.start, ctx.ID().getText(), visitExpr(ctx.expr()), ctx);
    }

    // Literals

    @Override
    public Literal visitLiteralInteger(CoolParser.LiteralIntegerContext ctx) {
        return new Literal(ctx.start, Literal.Type.INTEGER, ctx.getText());
    }

    @Override
    public Literal visitLiteralString(CoolParser.LiteralStringContext ctx) {
        return new Literal(ctx.start, Literal.Type.STRING, ctx.getText());
    }

    @Override
    public Literal visitLiteralTrue(CoolParser.LiteralTrueContext ctx) {
        return new Literal(ctx.start, Literal.Type.BOOLEAN, "true");
    }

    @Override
    public Literal visitLiteralFalse(CoolParser.LiteralFalseContext ctx) {
        return new Literal(ctx.start, Literal.Type.BOOLEAN, "false");
    }

    // Single-argument expressions

    @Override
    public IsVoid visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new IsVoid(ctx.start, visitExpr(ctx.expr()));
    }

    @Override
    public Unary visitNegate(CoolParser.NegateContext ctx) {
        return new Unary(ctx.start, Unary.Operation.findBySymbol("not"), visitExpr(ctx.expr()), ctx);
    }

    @Override
    public Instantiation visitInstantiation(CoolParser.InstantiationContext ctx) {
        return new Instantiation(ctx.start, ctx.TYPE().getText(), ctx);
    }

    // Others

    @Override
    public Block visitBlock(CoolParser.BlockContext ctx) {
        return new Block(ctx.start, visitAll(ctx.body, this::visitExpr));
    }

    @Override
    public MethodCall visitSelfMethodCall(CoolParser.SelfMethodCallContext ctx) {
        return new MethodCall(ctx.start, null, null, ctx.method.getText(), visitAll(ctx.args, this::visitExpr));
    }

    @Override
    public MethodCall visitMethodCall(CoolParser.MethodCallContext ctx) {
        return new MethodCall(ctx.start, visitExpr(ctx.obj), text(ctx.type), ctx.method.getText(), visitAll(ctx.args, this::visitExpr));
    }
}
