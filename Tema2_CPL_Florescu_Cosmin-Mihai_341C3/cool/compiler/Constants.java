package cool.compiler;

import cool.compiler.ast.Feature;
import cool.compiler.ast.Formal;
import cool.compiler.ast.Method;
import cool.compiler.ast.Class_c;

import java.util.*;

public class Constants {

    public static final String INT = "Int";
    public static final String STRING = "String";
    public static final String OBJECT = "Object";
    public static final String IO = "IO";

    public static final String SELF_TYPE = "SELF_TYPE";
    public static final String BOOL = "Bool";

    private static final List<Feature> objectFeatures =
        Arrays.asList(
            new Method(null, "abort", "Object", Collections.emptyList(), null, null),
            new Method(null, "type_name", "String", Collections.emptyList(), null, null),
            new Method(null, "copy", "SELF_TYPE", Collections.emptyList(), null, null)
        );

    public static Class_c OBJECT_CLASS_C =
        new Class_c(null, null, "Object", null, objectFeatures);

    private static final List<Feature> ioFeatures =
        Arrays.asList(
            new Method(null, "out_string", "SELF_TYPE",
                       List.of(new Formal(null, "x", "String", null)), null, null),
            new Method(null, "out_int", "SELF_TYPE",
                       List.of(new Formal(null, "x", "Int", null)), null, null),
            new Method(null, "in_string", "String",
                       Collections.emptyList(), null, null),
            new Method(null, "in_int", "Int",
                       Collections.emptyList(), null, null)
        );

    public static Class_c IO_CLASS_C =
        new Class_c(null, null, "IO", "Object", ioFeatures);

    public static Class_c INT_CLASS_C =
        new Class_c(null, null, "Int", "Object", new ArrayList<>());

    private static final List<Feature> stringFeatures =
        Arrays.asList(
            new Method(null, "length", "Int", null, null, null),
            new Method(null, "concat", "String",
                       List.of(new Formal(null, "s", "String", null)), null, null),
            new Method(null, "substr", "String",
                       List.of(new Formal(null, "i", "Int", null),
                               new Formal(null, "l", "Int", null)),
                       null, null)
        );

    public static Class_c STRING_CLASS_C =
        new Class_c(null, null, "String", "Object", stringFeatures);

    public static Class_c BOOL_CLASS_C =
        new Class_c(null, null, "Bool", "Object", new ArrayList<>());

    public static Class_c SELF_TYPE_CLASS_C =
        new Class_c(null, null, "SELF_TYPE", null, new ArrayList<>());
}
