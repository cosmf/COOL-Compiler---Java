package cool.compiler.generator;

import cool.compiler.Compiler;
import cool.compiler.ast.PClass;
import cool.compiler.ast.Program;
import cool.compiler.ast.expression.Literal;
import cool.structures.ClassSymbol;
import cool.structures.DefaultScope;
import cool.structures.MethodSymbol;
import cool.structures.SymbolTable;

import java.util.*;

public class GenerateCode {
        StringBuilder code = new StringBuilder();

        // Map that stores the ID of a class
        Map<Integer, String> idWithClass = new LinkedHashMap<>();

        // List containing all literals (constants) in the code
        public static List<Literal> literals = new ArrayList<>();
        // Map that stores the ID of a literal
        public static Map<String, Integer> idWithLiterals = new LinkedHashMap<>();

        public String generateCode(Program program){
            // Add false and true to the map
            idWithLiterals.put("false", 0);
            idWithLiterals.put("true", 1);
            code.append(Constants.generateGlobalCode());
            code.append(Constants.generateTags());

            generateStringConst(program);
            generateIntConst();
            code.append(Constants.generateBoolConstant());
            generateClassNameTab();
            generateClassObjTabl();
            generateProtObj(program);
            generateDispTab(program);
            code.append(Constants.generateHeapStart());
            code.append(Constants.generateBasicClassesInit());
            generateClassInit(program);
            generateFunctions(program);
            generateMainFunction(program);
            return code.toString();
        }

        public void generateStringConst(Program program){
            code.append(Constants.generateStringConstBase());
            var index = 6;
            // Generate constants for class names
            for(var cls : program.getClasses()){
                // Add class ID to the map
                idWithClass.put(index, cls.getName());
                // Create the code for the string constant
                code.append(Constants.generateStringConst(index, cls.getName()));
                index += 1;
            }
            // Generate constants for all literal strings (e.g., "abc", "Hello", "ok")
            for(var literal : literals){
                // Check if the literal is of type string
                if (Objects.requireNonNull(literal.getType()) == Literal.Type.STRING) {
                    // Generate the code
                    code.append(Constants.generateStringConst(index, literal.getContent()));
                    // Add it to the map for literals
                    idWithLiterals.put(literal.getContent(), index);
                    index += 1;
                }
            }
            // Add file names
            for(var pathName : Compiler.filePathNames){
                code.append(Constants.generateStringConst(index, pathName));
                idWithLiterals.put(pathName, index);
                index += 1;
            }
        }

        public void generateIntConst(){
            var index = 0;
            // Iterate through integer literals (e.g., 100, 2, 3, -3)
            for(var literal : literals){
                // Check if the literal is of type integer
                if(Objects.requireNonNull(literal.getType()) == Literal.Type.INTEGER){
                    // Add to the map of literals
                    code.append(Constants.generateIntConst(index, literal.getContent()));
                    idWithLiterals.put(literal.getContent(), index);
                    index += 1;
                }
            }
        }

        public void generateClassNameTab(){
            code.append("class_nameTab:\n");
            // Add str_const for basic classes
            for(int i = 0; i < 6;i++){
                code.append("    .word   str_const%s\n".formatted(i));
            }
            // Add str_const for other classes defined in the code
            for(int i : idWithClass.keySet()){
                code.append("    .word   str_const%s\n".formatted(i));
            }
        }

        public void generateClassObjTabl(){
            code.append("class_objTab:\n");
            // Add basic classes
            code.append(Constants.generateBasicClassesTab());
            // Iterate through IDs associated with user-defined classes
            for(int i : idWithClass.keySet()){
                // Retrieve the class associated with the ID
                var cls = idWithClass.get(i);
                // Generate code
                code.append("    .word   %s_protObj\n".formatted(cls));
                code.append("    .word   %s_init\n".formatted(cls));
            }
        }

        public void generateProtObj(Program program){
            code.append(Constants.generateBasicProtObj());
            for(var cls : program.getClasses()){
                int index = Constants.getKeyByValue(idWithClass, cls.getName());
                code.append(Constants.generateClassProtObj(index, cls));
            }
        }

        public void generateDispTab(Program program){
            code.append(Constants.generateBasicDispTab());
            for(var cls : program.getClasses()){
                code.append(Constants.generateClassDipsTab(cls));
            }
        }
        public void generateClassInit(Program program){
            // Iterate through all classes in the program
            for(var cls : program.getClasses()){
                // Add the associated label
                code.append("%s_init:\n".formatted(cls.getName()));
                // Add the constant part of initialization
                code.append(Constants.headInitClass());
                // If it has no parent, its parent is Object
                if(cls.getParent() == null){
                    code.append("    jal     Object_init\n");
                }
                // Otherwise, jump to its parent
                else {
                    code.append("    jal     %s_init\n".formatted(cls.getParent()));
                }
                // Add the constant footer part
                code.append(Constants.botInitClass());
            }
        }

        public void generateFunctions(Program program){
            for(var cls : program.getClasses()){
                var allMethods = cls.getClassMethods();
                for(var meth : allMethods.keySet()){
                    code.append("%s.%s:\n".formatted(cls.getName(), meth));
                    code.append(Constants.headFunctionInit());
                    EvalCalls.Eval(allMethods.get(meth).getBody(), code, cls.getName(), program);
                    code.append(Constants.botFunctionInit());
                }
            }
        }

        public void generateMainFunction(Program program){
            code.append("Main.main:\n");
            code.append(Constants.headFunctionInit());
            var mainFunction = SymbolTable.lookupClass("Main").getMethodScope().lookup("main");
            EvalCalls.Eval(mainFunction.getBody(), code, "Main", program);
            code.append(Constants.botFunctionInit());

        }

        public static int getLineFunction(String className, String methodName){
            Map<String, String> output1 = new LinkedHashMap<>();
            Map<String, String> output = new LinkedHashMap<>();
            ClassSymbol cls = SymbolTable.lookupClass(className);
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
            var index = 0;
            for(var key : output.keySet()){
                if(key.equals(methodName)){
                    return index * 4;
                }
                index += 1;
            }
            return -1;
        }
}
