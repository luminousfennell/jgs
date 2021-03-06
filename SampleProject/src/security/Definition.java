package security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.IAnnotationDAO;
import constraints.ComponentArrayRef;
import constraints.ComponentParameterRef;
import constraints.ComponentProgramCounterRef;
import constraints.ComponentReturnRef;
import constraints.IComponent;
import constraints.LEQConstraint;
import exception.AnnotationInvalidConstraintsException;
import security.ArrayCreator;

public class Definition extends ALevelDefinition {

    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Constraints {

        String[] value() default {};

    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FieldSecurity {

        String[] value();

    }

    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParameterSecurity {

        String[] value();

    }

    @Target({ ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReturnSecurity {

        String value();

    }

    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WriteEffect {

        String[] value();

    }

    public final class StringLevel extends ALevel {

        private final String level;

        public StringLevel(String level) {
            this.level = level;
        }

        @Override
        public String getName() {
            return level;
        }

        @Override
        public String toString() {
            return level;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((level == null) ? 0 : level.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StringLevel other = (StringLevel) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (level == null) {
                if (other.level != null)
                    return false;
            } else if (!level.equals(other.level))
                return false;
            return true;
        }

        private Definition getOuterType() {
            return Definition.this;
        }

    }

    private final ILevel low = new StringLevel("low");
    private final ILevel high = new StringLevel("high");

    public Definition() {
        super(FieldSecurity.class, ParameterSecurity.class,
                ReturnSecurity.class, WriteEffect.class, Constraints.class);
    }

    @Override
    public int compare(ILevel level1, ILevel level2) {
        if (level1.equals(level2)) {
            return 0;
        } else {
            if (level1.equals(high)) {
                return 1;
            } else {
                if (level1.equals(low) || level2.equals(high)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    @Override
    public ILevel getGreatesLowerBoundLevel() {
        return low;
    }

    @Override
    public ILevel getLeastUpperBoundLevel() {
        return high;
    }

    @Override
    public ILevel[] getLevels() {
        return new ILevel[] { low, high };
    }

    @Override
    public List<ILevel> extractFieldLevel(IAnnotationDAO dao) {
        List<ILevel> list = new ArrayList<ILevel>();
        for (String value : dao.getStringArrayFor("value")) {
            list.add(new StringLevel(value));
        }
        return list;
    }

    @Override
    public List<ILevel> extractParameterLevels(IAnnotationDAO dao) {
        List<ILevel> list = new ArrayList<ILevel>();
        for (String value : dao.getStringArrayFor("value")) {
            list.add(new StringLevel(value));
        }
        return list;
    }

    @Override
    public ILevel extractReturnLevel(IAnnotationDAO dao) {
        return new StringLevel(dao.getStringFor("value"));
    }

    @Override
    public List<ILevel> extractEffects(IAnnotationDAO dao) {
        List<ILevel> list = new ArrayList<ILevel>();
        for (String value : dao.getStringArrayFor("value")) {
            list.add(new StringLevel(value));
        }
        return list;
    }

    @Override
    public Set<LEQConstraint> extractConstraints(IAnnotationDAO dao,
                                                 String signature) {
        Set<LEQConstraint> constraints = new HashSet<LEQConstraint>();
        List<String> rawConstraints = dao.getStringArrayFor("value");
        for (String constraint : rawConstraints) {
            String errMsg =
                String.format("The specified constraint '%s' is invalid.",
                              constraint);
            if (constraint.contains("<=")) {
                String[] components = constraint.split("<=");
                if (components.length == 2) {
                    String lhs = components[0].trim();
                    String rhs = components[1].trim();
                    IComponent l =
                        convertIntoConstraintComponent(lhs, signature);
                    IComponent r =
                        convertIntoConstraintComponent(rhs, signature);
                    constraints.add(new LEQConstraint(l, r));
                } else {
                    throw new AnnotationInvalidConstraintsException(errMsg);
                }
            } else if (constraint.contains("=")) {
                String[] components = constraint.split("=");
                if (components.length == 2) {
                    String lhs = components[0].trim();
                    String rhs = components[1].trim();
                    IComponent l =
                        convertIntoConstraintComponent(lhs, signature);
                    IComponent r =
                        convertIntoConstraintComponent(rhs, signature);
                    constraints.add(new LEQConstraint(l, r));
                    constraints.add(new LEQConstraint(r, l));
                } else {
                    throw new AnnotationInvalidConstraintsException(errMsg);
                }
            } else if (constraint.contains(">=")) {
                String[] components = constraint.split(">=");
                if (components.length == 2) {
                    String lhs = components[0].trim();
                    String rhs = components[1].trim();
                    IComponent l =
                        convertIntoConstraintComponent(lhs, signature);
                    IComponent r =
                        convertIntoConstraintComponent(rhs, signature);
                    constraints.add(new LEQConstraint(r, l));
                } else {
                    throw new AnnotationInvalidConstraintsException(errMsg);
                }
            } else {
                throw new AnnotationInvalidConstraintsException(errMsg);
            }
        }
        return constraints;
    }

    private IComponent convertIntoConstraintComponent(String component,
                                                      String signature) {
        if (component.startsWith("@")) {
            String position = component.substring(1);
            if (position.startsWith("return")) {
                ComponentReturnRef returnRef =
                    new ComponentReturnRef(signature);
                if (position.equals("return")) {
                    return returnRef;
                } else {
                    String arrRef = position.replaceFirst("return", "");
                    if (isArrayReference(arrRef))
                        return new ComponentArrayRef(returnRef, arrRef.length());
                }
            } else if (position.equals("pc")) {
                return new ComponentProgramCounterRef(signature);
            } else if (Character.isDigit(position.charAt(0))) {
                int param = extractParameterPositionFromBeginOf(position);
                ComponentParameterRef paramRef =
                    new ComponentParameterRef(param, signature);
                if (position.equals(String.valueOf(param))) {
                    return paramRef;
                } else {
                    String arrRef =
                        position.replaceFirst(String.valueOf(param), "");
                    if (isArrayReference(arrRef))
                        return new ComponentArrayRef(paramRef, arrRef.length());
                }
            }
        }
        return new StringLevel(component);
    }

    private int extractParameterPositionFromBeginOf(String input) {
        String number = "";
        while (!input.isEmpty() && Character.isDigit(input.charAt(0))) {
            number += input.charAt(0);
            input = input.substring(1);
        }
        return Integer.valueOf(number).intValue();
    }

    private boolean isArrayReference(String input) {
        Pattern pattern = Pattern.compile("\\[*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    @ParameterSecurity({ "high" })
    @ReturnSecurity("high")
    @Constraints({ "@0 <= high", "high <= @return" })
    public static <T> T mkHigh(T object) {
        return object;
    }

    @ParameterSecurity({ "low" })
    @ReturnSecurity("low")
    @Constraints({ "@0 <= low", "low <= @return" })
    public static <T> T mkLow(T object) {
        return object;
    }

    @ArrayCreator("high")
    public static int[] arrayIntHigh(int capacity1) {
        return new int[capacity1];
    }

    @ArrayCreator({ "low" })
    public static int[] arrayIntLow(int capacity1) {
        return new int[capacity1];
    }

    @ArrayCreator({ "high", "low" })
    public static int[][] arrayTest(int capacity1, int capacity2) {
        return new int[capacity1][capacity2];
    }

    @ArrayCreator({ "low", "low" })
    public static int[][] arrayIntLowLow(int capacity1, int capacity2) {
        return new int[capacity1][capacity2];
    }

    @ArrayCreator({ "low", "high" })
    public static int[][] arrayIntLowHigh(int capacity1, int capacity2) {
        return new int[capacity1][capacity2];
    }

    @ArrayCreator({ "high", "high" })
    public static int[][] arrayIntHighHigh(int capacity1, int capacity2) {
        return new int[capacity1][capacity2];
    }

    @ArrayCreator({ "low" })
    public static Object[] arrayObjectLow(int capacity1) {
        return new Object[capacity1];
    }

    @ArrayCreator({ "low", "high", "high" })
    public static int[][][] arrayIntLowHighHigh(int capacity1,
                                                int capacity2,
                                                int capacity3) {
        return new int[capacity1][capacity2][capacity3];
    }

}
