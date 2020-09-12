package net.alagris;

import java.util.Stack;

public class CompilationError extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CompilationError(String msg) {
        super(msg);
    }

    public CompilationError(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CompilationError(Throwable cause) {
        super(cause);
    }

    public static final class DuplicateFunction extends CompilationError {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final Pos firstDeclaration;
        private final Pos secondDeclaration;
        private final String name;

        public DuplicateFunction(Pos firstDeclaration, Pos secondDeclaration,
                                 String name) {
            super(name + " is implemented at " + firstDeclaration + " and " + secondDeclaration);
            this.firstDeclaration = firstDeclaration;
            this.secondDeclaration = secondDeclaration;
            this.name = name;
        }

        public Pos getFirstDeclaration() {
            return firstDeclaration;
        }

        public Pos getSecondDeclaration() {
            return secondDeclaration;
        }

        public String getName() {
            return name;
        }

    }

    public static class ParseException extends CompilationError {
        private final Pos node;

        public ParseException(Pos node, Throwable cause) {
            super(cause);
            this.node = node;
        }

        public Pos getNode() {
            return node;
        }
    }

    public static class TypecheckException extends CompilationError {
        private final Pos funcPos, typePos;
        private final String name;

        public TypecheckException(Pos funcPos, Pos typePos, String name) {
            super("Function "+name+" "+funcPos+" does not conform to type "+typePos);
            this.funcPos = funcPos;
            this.typePos = typePos;
            this.name = name;
        }
    }

    public static class NondeterminismException extends CompilationError {
        private final Pos nondeterministicStatePos1, nondeterministicStatePos2;

        public NondeterminismException(Pos nondeterministicStatePos1, Pos nondeterministicStatePos2, String funcName) {
            super("Type "+funcName+" nondeterministically branches to "
                    +nondeterministicStatePos1+" and "+nondeterministicStatePos2);
            this.nondeterministicStatePos1 = nondeterministicStatePos1;
            this.nondeterministicStatePos2 = nondeterministicStatePos2;
            this.funcName = funcName;
        }

        private final String funcName;

    }

    public static class IllegalCharacter extends CompilationError {
        private final Pos pos;

        public IllegalCharacter(Pos pos, String s) {
            super(s);
            this.pos = pos;
        }
    }

    public static class WeightConflictingToThirdState extends CompilationError {

        private final LexUnicodeSpecification.FunctionalityCounterexampleToThirdState<
                LexUnicodeSpecification.E, LexUnicodeSpecification.P, ?> counterexample;

        public WeightConflictingToThirdState(LexUnicodeSpecification.FunctionalityCounterexampleToThirdState<
                LexUnicodeSpecification.E,LexUnicodeSpecification.P,?> counterexample) {
            super("Found weight conflicting transitions from "+counterexample.fromStateA+" and "+counterexample.fromStateB+" going to state "+counterexample.toStateC+
                    " over transitions "+counterexample.overEdgeA+" and "+counterexample.overEdgeB);
            this.counterexample = counterexample;
        }

    }

    public static class WeightConflictingFinal extends CompilationError {

        private final LexUnicodeSpecification.FunctionalityCounterexampleFinal<
                LexUnicodeSpecification.E, LexUnicodeSpecification.P, ?> counterexample;

        public WeightConflictingFinal(LexUnicodeSpecification.FunctionalityCounterexampleFinal<
                LexUnicodeSpecification.E,LexUnicodeSpecification.P,?> counterexample) {
            super("Found weight conflicting final states "+counterexample.fromStateA+" and "+counterexample.fromStateB+
                    " with state output "+counterexample.finalEdgeA+" and "+counterexample.finalEdgeA);
            this.counterexample = counterexample;
        }

    }
}