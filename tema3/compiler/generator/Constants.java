package cool.compiler.generator;

import cool.compiler.ast.Method;
import cool.compiler.ast.PClass;
import cool.compiler.ast.Program;
import cool.compiler.ast.expression.Literal;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Constants {
    public static String generateGlobalCode(){
        return """
                    .data
                    .align  2
                    .globl  class_nameTab
                    .globl  Int_protObj
                    .globl  String_protObj
                    .globl  bool_const0
                    .globl  bool_const1
                    .globl  Main_protObj
                    .globl  _int_tag
                    .globl  _string_tag
                    .globl  _bool_tag
                """;
    }

    public static String generateTags(){
        return """
                _int_tag:
                    .word   2
                _string_tag:
                    .word   3
                _bool_tag:
                    .word   4
                """;
    }

    public static String generateStringConstBase(){
        return """
                str_const0:
                    .word   3
                    .word   5
                    .word   String_dispTab
                    .word   0
                    .asciiz ""
                    .align  2
                str_const1:
                    .word   3
                    .word   6
                    .word   String_dispTab
                    .word   6
                    .asciiz "Object"
                    .align  2
                str_const2:
                    .word   3
                    .word   5
                    .word   String_dispTab
                    .word   2
                    .asciiz "IO"
                    .align  2
                str_const3:
                    .word   3
                    .word   5
                    .word   String_dispTab
                    .word   3
                    .asciiz "Int"
                    .align  2
                str_const4:
                    .word   3
                    .word   6
                    .word   String_dispTab
                    .word   6
                    .asciiz "String"
                    .align  2
                str_const5:
                    .word   3
                    .word   6
                    .word   String_dispTab
                    .word   4
                    .asciiz "Bool"
                    .align  2
                """;
    }

    public static String generateStringConst(int index, String content){
        var classNameLen = content.length();
        var secondWord = (content.length() + 1) / 4 + 5;
        return ("""
                str_const%s:
                    .word   3
                    .word   %s
                    .word   String_dispTab
                    .word   %s
                    .asciiz "%s"
                    .align  2
                """).formatted(index, secondWord, classNameLen, content);
    }

    public static String generateIntConst(int index, String content){
        return ("""
                int_const%s:
                    .word   2
                    .word   4
                    .word   Int_dispTab
                    .word   %s
                """).formatted(index, content);
    }

    public static String generateIntConst(){
        return "";
    }

    public static String generateBoolConstant(){
        return """
                bool_const0:
                    .word   4
                    .word   4
                    .word   Bool_dispTab
                    .word   0
                bool_const1:
                    .word   4
                    .word   4
                    .word   Bool_dispTab
                    .word   1
                """;
    }

    public static String generateBasicClassesTab(){
        return """
                    .word   Object_protObj
                    .word   Object_init
                    .word   IO_protObj
                    .word   IO_init
                    .word   Int_protObj
                    .word   Int_init
                    .word   String_protObj
                    .word   String_init
                    .word   Bool_protObj
                    .word   Bool_init
                """;
    }

    public static String generateBasicProtObj(){
        return """
                Object_protObj:
                    .word   1
                    .word   3
                    .word   Object_dispTab
                                
                IO_protObj:
                    .word   2
                    .word   3
                    .word   IO_dispTab
                                
                Int_protObj:
                    .word   3
                    .word   4
                    .word   Int_dispTab
                    .word   0
                String_protObj:
                    .word   4
                    .word   5
                    .word   String_dispTab
                    .word   0
                    .asciiz ""
                    .align  2
                Bool_protObj:
                    .word   5
                    .word   4
                    .word   Bool_dispTab
                    .word   0
                """;
    }

    public static String generateClassProtObj(int index, PClass pClass){
        StringBuilder clsProtObj = new StringBuilder();
        var allAttributes = pClass.getAllAttributes();
        clsProtObj.append("""
                %s_protObj:
                    .word   %s
                    .word   3
                    .word   %s_dispTab
                """.formatted(pClass.getName(), index, pClass.getName()));
        for(var attr : allAttributes.keySet()){
            VariableSymbol variableSymbol = allAttributes.get(attr);
            if(variableSymbol.getInitializer() == null)
                switch (variableSymbol.getType().getName()) {
                    case "Int" -> clsProtObj.append("    .word   0\n");
                    case "String" -> clsProtObj.append("    .word   str_const0\n");
                    case "Bool" -> clsProtObj.append("    .word   bool_const0\n");
                    case "SELF_TYPE" -> clsProtObj.append("    .word   0\n");
                }
            else {
                if(variableSymbol.getInitializer() instanceof Literal literal){
                    switch (variableSymbol.getType().getName()) {
                        case "Int" -> clsProtObj.append("    .word   int_const%s\n"
                                .formatted(GenerateCode.idWithLiterals.get(literal.getContent())));
                        case "String" -> clsProtObj.append("    .word   str_const%s\n"
                                .formatted(GenerateCode.idWithLiterals.get(literal.getContent())));
                        case "Bool" -> clsProtObj.append("    .word   bool_const%s\n"
                                .formatted(GenerateCode.idWithLiterals.get(literal.getContent())));
                        case "SELF_TYPE" -> clsProtObj.append("    .word   0\n");
                    }
                }
            }
        }
        return clsProtObj.toString();
    }

    public static String generateBasicDispTab(){
        return """
                Object_dispTab:
                    .word   Object.abort
                    .word   Object.type_name
                    .word   Object.copy
                IO_dispTab:
                    .word   Object.abort
                    .word   Object.type_name
                    .word   Object.copy
                    .word   IO.out_string
                    .word   IO.out_int
                    .word   IO.in_string
                    .word   IO.in_int
                Int_dispTab:
                    .word   Object.abort
                    .word   Object.type_name
                    .word   Object.copy
                String_dispTab:
                    .word   Object.abort
                    .word   Object.type_name
                    .word   Object.copy
                    .word   String.length
                    .word   String.concat
                    .word   String.substr
                Bool_dispTab:
                    .word   Object.abort
                    .word   Object.type_name
                    .word   Object.copy
                """;
    }

    public static String generateClassDipsTab(PClass pClass){
        StringBuilder clsDispBuilder = new StringBuilder();
        clsDispBuilder.append("%s_dispTab:\n".formatted(pClass.getName()));
        var allMethods = pClass.getAllMethods();
        for(String meth : allMethods.keySet()){
            clsDispBuilder.append("    .word   %s.%s\n".formatted(allMethods.get(meth), meth));
        }
        return clsDispBuilder.toString();
    }

    public static String generateHeapStart(){
        return """
                heap_start:
                    .word   0
                    .text
                    .globl  Int_init
                    .globl  String_init
                    .globl  Bool_init
                    .globl  Main_init
                    .globl  Main.main
                """;
    }

    public static String generateBasicClassesInit(){
        return """
                Object_init:
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                IO_init:
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                    jal     Object_init
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                Int_init:
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                    jal     Object_init
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                String_init:
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                    jal     Object_init
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                Bool_init:
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                    jal     Object_init
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                """;
    }

    public static String headInitClass(){
        return """
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                """;
    }

    public static String botInitClass(){
        return """
                    move    $a0 $s0
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                """;
    }

    public static String headFunctionInit(){
        return """
                    addiu   $sp $sp -12
                    sw      $fp 12($sp)
                    sw      $s0 8($sp)
                    sw      $ra 4($sp)
                    addiu   $fp $sp 4
                    move    $s0 $a0
                """;
    }

    public static String botFunctionInit(){
        return """
                    lw      $fp 12($sp)
                    lw      $s0 8($sp)
                    lw      $ra 4($sp)
                    addiu   $sp $sp 12
                    jr      $ra
                """;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


}
