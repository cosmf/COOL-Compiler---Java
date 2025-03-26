package cool.compiler;

import cool.compiler.ast.*;
import cool.compiler.ast.expression.*;
import cool.compiler.generator.GenerateCode;
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
        return new Program(ctx, visitAll(ctx.class_(), this::visitClass));
    }

    @Override
    public PClass visitClass(CoolParser.ClassContext ctx) {
        return new PClass(ctx, ctx.name.getText(), text(ctx.parent), ctx.feature().stream().map(c -> (Feature) visit(c)).collect(Collectors.toList()));
    }

    @Override
    public Attribute visitAttribute(CoolParser.AttributeContext ctx) {
        return new Attribute(ctx, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()));
    }

    @Override
    public Method visitMethod(CoolParser.MethodContext ctx) {
        return new Method(ctx, ctx.ID().getText(), ctx.TYPE().getText(), visitAll(ctx.args, this::visitFormal), visitExpr(ctx.expr()));
    }

    @Override
    public Formal visitFormal(CoolParser.FormalContext ctx) {
        return new Formal(ctx, ctx.ID().getText(), ctx.TYPE().getText());
    }

    // Complex expressions

    @Override
    public If visitIf(CoolParser.IfContext ctx) {
        return new If(ctx, visitExpr(ctx.cond), visitExpr(ctx.thenBranch), visitExpr(ctx.elseBranch));
    }

    @Override
    public While visitWhile(CoolParser.WhileContext ctx) {
        return new While(ctx, visitExpr(ctx.cond), visitExpr(ctx.body));
    }

    @Override
    public Let visitLet(CoolParser.LetContext ctx) {
        return new Let(ctx, visitAll(ctx.vars, this::visitLocal), visitExpr(ctx.body));
    }

    @Override
    public LetLocal visitLocal(CoolParser.LocalContext ctx) {
        return new LetLocal(ctx, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()));
    }

    @Override
    public Case visitCase(CoolParser.CaseContext ctx) {
        return new Case(ctx, visitExpr(ctx.expr()), visitAll(ctx.case_branch(), this::visitCase_branch));
    }

    @Override
    public CaseBranch visitCase_branch(CoolParser.Case_branchContext ctx) {
        return new CaseBranch(ctx, ctx.ID().getText(), ctx.TYPE().getText(), visitExpr(ctx.expr()));
    }

    // Arithmetic & comparison

    @Override
    public Arithmetic visitArithmetic(CoolParser.ArithmeticContext ctx) {
        return new Arithmetic(ctx, Arithmetic.Op.findBySymbol(ctx.op.getText()), visitExpr(ctx.left), visitExpr(ctx.right));
    }

    @Override
    public Complement visitUnary(CoolParser.UnaryContext ctx) {
        return new Complement(ctx, visitExpr(ctx.expr()));
    }

    @Override
    public Comparison visitComparison(CoolParser.ComparisonContext ctx) {
        return new Comparison(ctx, Comparison.Op.findBySymbol(ctx.op.getText()), visitExpr(ctx.left), visitExpr(ctx.right));
    }

    // Variable stuff

    @Override
    public Variable visitVar(CoolParser.VarContext ctx) {
        return new Variable(ctx, ctx.ID().getText());
    }

    @Override
    public Assign visitVarAssign(CoolParser.VarAssignContext ctx) {
        return new Assign(ctx, ctx.ID().getText(), visitExpr(ctx.expr()));
    }

    // Literals

    @Override
    public Literal visitLiteralInteger(CoolParser.LiteralIntegerContext ctx) {
        Literal lt = new Literal(ctx, Literal.Type.INTEGER, ctx.getText());
        GenerateCode.literals.add(lt);
        return lt;
    }

    @Override
    public Literal visitLiteralString(CoolParser.LiteralStringContext ctx) {
        Literal lt = new Literal(ctx, Literal.Type.STRING, ctx.getText());
        GenerateCode.literals.add(lt);
        return lt;
    }

    @Override
    public Literal visitLiteralTrue(CoolParser.LiteralTrueContext ctx) {
        Literal lt = new Literal(ctx, Literal.Type.BOOLEAN, "true");
        GenerateCode.literals.add(lt);
        return lt;
    }

    @Override
    public Literal visitLiteralFalse(CoolParser.LiteralFalseContext ctx) {
        Literal lt = new Literal(ctx, Literal.Type.BOOLEAN, "false");
        GenerateCode.literals.add(lt);
        return lt;
    }

    // Single-argument expressions

    @Override
    public IsVoid visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new IsVoid(ctx, visitExpr(ctx.expr()));
    }

    @Override
    public Negate visitNegate(CoolParser.NegateContext ctx) {
        return new Negate(ctx, visitExpr(ctx.expr()));
    }

    @Override
    public Instantiation visitInstantiation(CoolParser.InstantiationContext ctx) {
        return new Instantiation(ctx, ctx.TYPE().getText());
    }

    // Others

    @Override
    public Block visitBlock(CoolParser.BlockContext ctx) {
        return new Block(ctx, visitAll(ctx.body, this::visitExpr));
    }

    @Override
    public SelfMethodCall visitSelfMethodCall(CoolParser.SelfMethodCallContext ctx) {
        return new SelfMethodCall(ctx, ctx.method.getText(), visitAll(ctx.args, this::visitExpr));
    }

    @Override
    public MethodCall visitMethodCall(CoolParser.MethodCallContext ctx) {
        return new MethodCall(ctx, visitExpr(ctx.obj), text(ctx.type), ctx.method.getText(), visitAll(ctx.args, this::visitExpr));
    }
}
