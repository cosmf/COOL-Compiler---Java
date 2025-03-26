package cool.compiler.generator;

import cool.compiler.ast.Method;
import cool.compiler.ast.PClass;
import cool.compiler.ast.Program;
import cool.compiler.ast.expression.*;
import cool.structures.DefaultScope;
import cool.structures.SymbolTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static cool.compiler.generator.GenerateCode.getLineFunction;
import static cool.compiler.generator.GenerateCode.idWithLiterals;

public class EvalCalls {

    // Map to store method dispatch IDs
    public static final Map<String, Integer> dispatchWithId = new LinkedHashMap<>();
    public static int dispatchId = 0;

    /**
     * Evaluates an expression and generates assembly code for it.
     * @param expression The expression to evaluate.
     * @param code The StringBuilder to append generated code to.
     * @param className The current class name.
     * @param program The entire program containing all classes and methods.
     * @return The type of the evaluated expression.
     */
    public static String Eval(Expression expression, StringBuilder code, String className, Program program){
        PClass pclass = new PClass(null, null, null, new ArrayList<>());
        // Find the corresponding class object by name
        for(PClass cls : program.getClasses()){
            if(cls.getName().equals(className)){
                pclass = cls;
                break;
            }
        }

        // Handle variable expressions
        if(expression instanceof Variable variable){
            if(variable.getId().equals("self")){
                code.append("    move    $a0 $s0\n"); // Load the current object into $a0
            } else {
                var attributeList = pclass.getAllAttributes();
                var index = 0;
                for(var attr : attributeList.keySet()){
                    if(attr.equals(variable.getId())){
                        break;
                    }
                    index += 1;
                }
                code.append("    lw      $a0 %s($s0)\n".formatted((index + 3) * 4)); // Load the attribute value
            }
            return className;
        }

        // Handle literal values
        if(expression instanceof Literal literal){
            switch (literal.getType()) {
                case STRING -> {
                    code.append("    la      $a0 str_const%s\n"
                            .formatted(GenerateCode.idWithLiterals.get(literal.getContent()))); // Load string literal
                }
                case INTEGER -> code.append("    la      $a0 int_const%s\n"
                        .formatted(GenerateCode.idWithLiterals.get(literal.getContent()))); // Load integer literal
                case BOOLEAN -> code.append("    la      $a0 bool_const%s\n"
                        .formatted(GenerateCode.idWithLiterals.get(literal.getContent()))); // Load boolean literal
            }

            return literal.getExpressionType(null).getName();
        }

        // Handle method calls
        if(expression instanceof MethodCall methodCall){
            for(var expr: methodCall.getArguments()){
                Eval(expr, code, className, program);
                code.append("    sw      $a0 0($sp)\n"); // Push argument onto the stack
                code.append("    addiu   $sp $sp -4\n"); // Adjust the stack pointer
            }
            var leftType = Eval(methodCall.getTargetObject(), code, className, program);
            code.append("    bnez    $a0 dispatch%s\n".formatted(dispatchId)); // Check for null object
            code.append("    la      $a0 str_const%s\n"
                    .formatted(idWithLiterals.get(SymbolTable.getFilePath(methodCall.getContext()))));
            code.append("    li      $t1 %s\n".formatted(28)); // Error code
            code.append("    jal     _dispatch_abort\n"); // Call error handler
            code.append("dispatch%s:\n".formatted(dispatchId));
            code.append("    lw      $t1 8($a0)\n"); // Load the dispatch table
            code.append("    lw      $t1 %s($t1)\n".formatted(getLineFunction(leftType, methodCall.getName()))); // Get method address
            code.append("    jalr    $t1\n"); // Jump to the method
            dispatchWithId.put(className + "." + methodCall.getName(), dispatchId);
            dispatchId += 1;
            return getMethodReturnType(leftType, methodCall.getName());
        }

        // Handle self-method calls
        if(expression instanceof SelfMethodCall selfMethodCall){
            for(var expr: selfMethodCall.getArguments()){
                Eval(expr, code, className, program);
                code.append("    sw      $a0 0($sp)\n"); // Push argument onto the stack
                code.append("    addiu   $sp $sp -4\n"); // Adjust the stack pointer
            }
            code.append("    move    $a0 $s0\n"); // Load the current object
            code.append("    bnez    $a0 dispatch%s\n".formatted(dispatchId)); // Check for null object
            code.append("    la      $a0 str_const%s\n"
                    .formatted(idWithLiterals.get(SymbolTable.getFilePath(selfMethodCall.getCtx()))));
            code.append("    li      $t1 %s\n".formatted(28)); // Error code
            code.append("    jal     _dispatch_abort\n"); // Call error handler
            code.append("dispatch%s:\n".formatted(dispatchId));
            code.append("    lw      $t1 8($a0)\n"); // Load the dispatch table
            code.append("    lw      $t1 %s($t1)\n".formatted(getLineFunction(className,
                    selfMethodCall.getName()))); // Get method address
            code.append("    jalr    $t1\n"); // Jump to the method
            dispatchWithId.put(className + "." + selfMethodCall.getName(), dispatchId);
            dispatchId += 1;
            return getMethodReturnType(className, selfMethodCall.getName());
        }

        // Handle blocks of expressions
        if(expression instanceof Block block){
            for(var expr : block.getExpressions()){
                Eval(expr, code, className, program); // Evaluate each expression in the block
            }
        }
        return "Object"; // Default return type
    }

    /**
     * Retrieves the return type of a method.
     * @param className The class name containing the method.
     * @param methodName The method name.
     * @return The return type of the method as a string.
     */
    private static String getMethodReturnType(String className, String methodName){
        if(methodName.equals("out_string") || methodName.equals("out_int")){
            return "IO"; // Special case for IO methods
        }
        return SymbolTable.lookupClass(className).getMethodScope()
                .lookup(methodName).getReturnType().getName();
    }
}
