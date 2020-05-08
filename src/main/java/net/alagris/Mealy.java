package net.alagris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.alagris.Glushkov.OutputWeight;
import net.alagris.Glushkov.Renamed;

public class Mealy {

    public static class Transition {
        /**
         * If inputFromInclusive>inputToInclusive then it's an epsilon transition.
         */
        int inputFromInclusive, inputToInclusive;
        /**
         * epsilon is encoded as "", empty language is encoded as null. By default all
         * transitions output empty language. Concatenation of empty language with
         * anything, yields empty language. If automaton returns empty language as
         * output, then it is equivalent to rejection. Therefore those two facts imply
         * that if automaton returns empty language at any point, then it rejects (or in
         * non-deterministic case, the particular computation branch rejects)
         */
        String output;
    }

    public static class Tran {
        final int inputFromInclusive, inputToInclusive, toState, weight;
        final String output;

        public Tran(int weight, int inputFromInclusive, int inputToInclusive, int toState, String output) {
            this.inputFromInclusive = inputFromInclusive;
            this.inputToInclusive = inputToInclusive;
            this.toState = toState;
            this.output = output;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "[" + (char) inputFromInclusive + "-" + (char) inputToInclusive + "] " + toState + " \"" + output
                    + "\"";
        }
    }

    /** epsilon-free transitions! */
    final Tran[][] tranisitons;
    final String[] mooreOutput;
    final int[] mooreWeights;
    final int initialState;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tranisitons.length; i++) {
            final Tran[] t = tranisitons[i];
            for (int j = 0; j < t.length; j++) {
                sb.append(i).append(" ").append(t[j].toString()).append("\n");
            }
        }
        for (int i = 0; i < mooreOutput.length; i++) {
            sb.append(i).append(" \"").append(mooreOutput[i]).append("\"\n");
        }
        sb.append(initialState);
        return sb.toString();
    }

    public static Mealy compile(String emptyWordOutput, int emptyWordWeight, HashMap<Integer, OutputWeight> startStates,
            String[][] outputMatrix, int[][] weightMatrix, HashMap<Integer, OutputWeight> endStates,
            Renamed[] indexToState) {

        final int stateCount = outputMatrix.length + 1;
        final int initialState = outputMatrix.length;
        final Tran[][] transitions = new Tran[stateCount][];
        for (int state = 0; state < outputMatrix.length; state++) {
            final String[] outgoing = outputMatrix[state];
            final int[] weights = weightMatrix[state];
            final ArrayList<Tran> filteredOutgoing = new ArrayList<>();
            assert outgoing.length == outputMatrix.length : "Length " + outgoing.length + " != " + outputMatrix.length;
            assert outgoing.length == transitions.length - 1 : outgoing.length + " != " + (transitions.length - 1);
            for (int target = 0; target < outgoing.length; target++) {
                final String tranOutput = outgoing[target];
                final int tranWeight = weights[target];
                if (tranOutput != null) {
                    final Renamed targetState = indexToState[target];
                    filteredOutgoing
                            .add(new Tran(tranWeight, targetState.inputFrom, targetState.inputTo, target, tranOutput));
                } else {
                    assert tranWeight == 0 : tranWeight;
                }
            }
            filteredOutgoing.sort((a, b) -> Integer.compare(a.inputFromInclusive, b.inputFromInclusive));
            transitions[state] = filteredOutgoing.toArray(new Tran[0]);
        }
        final Tran[] initialTransitions = new Tran[startStates.size()];
        {
            int i = 0;
            for (Entry<Integer, OutputWeight> startEntry : startStates.entrySet()) {
                final int toState = startEntry.getKey();
                final int weight = startEntry.getValue().weight();
                final Renamed original = indexToState[toState];
                initialTransitions[i++] = new Tran(weight, original.inputFrom, original.inputTo, toState,
                        startEntry.getValue().getOutput());
            }
            Arrays.sort(initialTransitions, (a, b) -> Integer.compare(a.inputFromInclusive, b.inputFromInclusive));
        }
        transitions[initialState] = initialTransitions;
        final String[] mooreOutput = new String[transitions.length];
        final int[] mooreWeights = new int[transitions.length];
        mooreOutput[initialState] = emptyWordOutput;
        mooreWeights[initialState] = emptyWordWeight;
        for (Entry<Integer, OutputWeight> endEntry : endStates.entrySet()) {
            mooreOutput[endEntry.getKey()] = endEntry.getValue().getOutput();
            mooreWeights[endEntry.getKey()] = endEntry.getValue().weight();
        }
        return new Mealy(transitions, mooreOutput, mooreWeights, initialState);
    }

    public static class Bits extends BitSet {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private final int len;

        public Bits(int len) {
            super(len);
            this.len = len;
        }

        public int len() {
            return len;
        }

    }

    public void checkForNondeterminism() {
        final HashSet<String> visitedSuperpositions = new HashSet<>();
        final Stack<Bits> toVisit = new Stack<>();
        final Bits initial = new Bits(stateCount());
        initial.set(initialState);
        toVisit.push(initial);
        while (!toVisit.isEmpty()) {
            final Bits next = toVisit.pop();
            checkIfSuperpositionAcceptsInMultiplePlaces(next);
            final HashSet<Integer> inputsThatNeedChecking = superpositionAllTransitionsAligned(next);
            for (int input : inputsThatNeedChecking) {
                final Bits afterTransitioning = superpositionTransition(next, input);
                final String serializedAfterTransitioning = afterTransitioning.toString();
                if (visitedSuperpositions.add(serializedAfterTransitioning)) {
                    toVisit.add(afterTransitioning);
                }
            }
        }
    }

    public void checkIfSuperpositionAcceptsInMultiplePlaces(Bits superposition) {
        final HashMap<Integer,Integer> weights = new HashMap<>();
        for (int state : (Iterable<Integer>) () -> superposition.stream().iterator()) {
            if (mooreOutput[state] != null) {
                final Integer conflictingState = weights.put(mooreWeights[state], state);
                if (conflictingState!=null) {
                    throw new IllegalStateException("It's possible to accept both " + state + " and " + conflictingState);
                }
            }
        }
    }

    /**
     * Returns list of all inputs at which something changes among transitions. For
     * instance, if all states in the superposition have only one transition [a-h],
     * then the output will contain only [a,h+1]. But if there is at least one state
     * which has different transition, say [e-o], then the output will contain
     * [a,e,h+1,o+1].
     */
    public HashSet<Integer> superpositionAllTransitionsAligned(Bits superposition) {
        final HashSet<Integer> checkPoints = new HashSet<>();
        checkPoints.add(Integer.MIN_VALUE);
        for (int state : (Iterable<Integer>) () -> superposition.stream().iterator()) {
            for (Tran transition : tranisitons[state]) {
                checkPoints.add(transition.inputFromInclusive);
                checkPoints.add(transition.inputToInclusive + 1);
            }
        }
        return checkPoints;
    }

    public Bits superpositionTransition(Bits superposition, int input) {
        assert superposition.len() == stateCount() : "Wrong size " + superposition.length() + " != " + stateCount();
        final Bits output = new Bits(stateCount());
        final Bits conflicts = new Bits(stateCount());
        for (int state : (Iterable<Integer>) () -> superposition.stream().iterator()) {
            It<Tran> transitionsTaken = transitionsFor(state, input);
            Tran next = transitionsTaken.next();
            while (next != null) {
                if (output.get(next.toState)) {
                    conflicts.set(next.toState);
                }else {
                    output.set(next.toState);
                }
                next = transitionsTaken.next();
            }
        }
        
        for (int destinationState : (Iterable<Integer>) () -> conflicts.stream().iterator()) {
            final HashMap<Integer, Integer> weightToSourceState = new HashMap<>();  
            for (int sourceState : (Iterable<Integer>) () -> superposition.stream().iterator()) {
                final Tran t = transitionFor(sourceState, input, destinationState);
                if(t!=null) {
                    final Integer conflictingSourceState = weightToSourceState.put(t.weight, sourceState);
                    if(conflictingSourceState!=null) {
                        throw new IllegalStateException(
                                "Superposition " + superposition + " has two conflicting transitions to " + destinationState
                                        + " over input " + input + ". One comes from " + sourceState+" the other from "+conflictingSourceState);
                    }       
                }
            }
        }
        
        return output;
    }

    public interface It<T> {
        T next();
    }
    
    public Tran transitionFor(int sourceState, int input, int destinationState) {
        final It<Tran> trans = transitionsFor(sourceState, input);
        Tran t;
        while((t=trans.next())!=null) {
            if(t.toState==destinationState)return t;
        }
        return null;
        
    }

    public It<Tran> transitionsFor(int state, int input) {
        final Tran[] trans = tranisitons[state];
        int i = trans.length - 1;
        for (; i >= 0; i--) {
            if (trans[i].inputFromInclusive <= input) {
                break;
            }
        }
        final int last = i;
        return new It<Tran>() {
            int index = last;

            @Override
            public Tran next() {
                if (index < 0)
                    return null;
                if (input <= trans[index].inputToInclusive) {
                    return trans[index--];
                } else {
                    return null;
                }
            }
        };
    }

    private int stateCount() {
        return tranisitons.length;
    }

    public String evaluate(String input) {
        final List<Integer> list = input.codePoints().boxed().collect(Collectors.toList());
        return evaluate(list.size(), list.iterator());
    }

    private static class Backtrack {
        int sourceState;
        int weight;
        String transitionOutput;

        public Backtrack(int sourceState,int weight, String transitionOutput) {
            this.sourceState = sourceState;
            this.weight = weight;
            this.transitionOutput = transitionOutput;
        }

        @Override
        public String toString() {
            return sourceState + " \"" + transitionOutput + "\"";
        }

    }

    public String evaluate(int inputLength, Iterator<Integer> input) {

        final Backtrack[][] superpositionComputation = new Backtrack[inputLength + 1][];
        for (int i = 0; i < superpositionComputation.length; i++) {
            superpositionComputation[i] = new Backtrack[stateCount()];
        }
        superpositionComputation[0][initialState] = new Backtrack(-1, -1, null);
        for (int charIndex = 0; charIndex < inputLength; charIndex++) {
            final int nextChar = input.next();
            final Backtrack[] fromSuperposition = superpositionComputation[charIndex];
            final Backtrack[] toSuperposition = superpositionComputation[charIndex + 1];
            for (int state = 0; state < stateCount(); state++) {
                if (fromSuperposition[state] != null) {
                    final It<Tran> transitions = transitionsFor(state, nextChar);
                    Tran nextTran = transitions.next();
                    while (nextTran != null) {
                        Backtrack prev = toSuperposition[nextTran.toState];
                        if (prev == null) {
                            toSuperposition[nextTran.toState] = new Backtrack(state, nextTran.weight, nextTran.output);
                        }else if(nextTran.weight==prev.weight){
                            throw new IllegalStateException("Reached state " + nextTran.toState + " from both " + state
                                    + " (outputting " + nextTran.output + ") and "
                                    + prev.sourceState + " (outputting "
                                    + prev.transitionOutput + ")");
                        }else if(nextTran.weight>prev.weight) {
                            prev.weight = nextTran.weight;
                            prev.transitionOutput = nextTran.output;
                            prev.sourceState = state;
                        }
                        nextTran = transitions.next();
                    }

                }
            }
        }
        final Backtrack[] last = superpositionComputation[inputLength];
        int acceptingState = -1;
        int acceptingWeight = -1;
        for (int state = 0; state < stateCount(); state++) {
            if (last[state] != null && mooreOutput[state] != null) {
                if (mooreWeights[state] > acceptingWeight) {
                    acceptingState = state;
                    acceptingWeight = mooreWeights[state];
                } else if(mooreWeights[state] == acceptingWeight){
                    throw new IllegalStateException(
                            "Both states " + acceptingState + " and " + state + " accepted simultaneously!");
                }
            }
        }
        if (acceptingState == -1)
            return null;
        final StringBuilder output = new StringBuilder(mooreOutput[acceptingState]);
        int backtrackedState = acceptingState;
        for (int charIndex = inputLength; charIndex > 0; charIndex--) {
            Backtrack pointer = superpositionComputation[charIndex][backtrackedState];
            backtrackedState = pointer.sourceState;
            output.insert(0, pointer.transitionOutput);
        }
        assert backtrackedState == initialState;
        return output.toString();

    }

    public Mealy(Tran[][] transitions, String[] mooreOutput, int[] mooreWeights, int initialState) {
        assert 0 <= initialState && initialState < mooreOutput.length;
        assert transitions.length == mooreOutput.length;
        this.tranisitons = transitions;
        this.mooreOutput = mooreOutput;
        this.mooreWeights = mooreWeights;
        this.initialState = initialState;
    }
}
