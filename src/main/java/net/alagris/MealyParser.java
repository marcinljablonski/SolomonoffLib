/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package net.alagris;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PrimitiveIterator.OfInt;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.alagris.GrammarBaseVisitor;
import net.alagris.GrammarLexer;
import net.alagris.GrammarParser;
import net.alagris.GrammarParser.AtomicLiteralContext;
import net.alagris.GrammarParser.AtomicNestedContext;
import net.alagris.GrammarParser.AtomicRangeContext;
import net.alagris.GrammarParser.AtomicVarIDContext;
import net.alagris.GrammarParser.EndConcatContext;
import net.alagris.GrammarParser.EndFuncsContext;
import net.alagris.GrammarParser.EndParamsContext;
import net.alagris.GrammarParser.EndUnionContext;
import net.alagris.GrammarParser.EpsilonProductContext;
import net.alagris.GrammarParser.Func_defContext;
import net.alagris.GrammarParser.KleeneClosureContext;
import net.alagris.GrammarParser.MoreConcatContext;
import net.alagris.GrammarParser.MoreFuncsContext;
import net.alagris.GrammarParser.MoreParamsContext;
import net.alagris.GrammarParser.MoreUnionContext;
import net.alagris.GrammarParser.NoKleeneClosureContext;
import net.alagris.GrammarParser.ProductContext;
import net.alagris.GrammarParser.StartContext;
import net.alagris.Simple.A;
import net.alagris.WithVars.V;

public class MealyParser {
    static void repeat(StringBuilder sb, String s, int times) {
        while (times-- > 0) {
            sb.append(s);
        }
    }

    static void ind(StringBuilder sb, int indent) {
        repeat(sb, "    ", indent);
    }

    public static int escapeCharacter(int c) {
        switch (c) {
        case 'b':
            return '\b';
        case 'n':
            return '\n';
        case 'r':
            return '\r';
        case 't':
            return '\t';
        case 'f':
            return '\f';
        default:
            return c;
        }
    }

    interface AST {

    }

    static class Params implements AST {
        final ArrayList<String> params = new ArrayList<>();
    }

    static class Funcs implements AST {
        final ArrayList<Func> funcs = new ArrayList<>();

    }

    static class Func implements AST {
        final String name;
        final String[] vars;
        final V body;

        public Func(String name, String[] vars, V body) {
            this.name = name;
            this.vars = vars;
            this.body = body;
        }

    }

    private static class GrammarVisitor extends GrammarBaseVisitor<AST> {

        @Override
        public AST visitEndParams(EndParamsContext ctx) {
            return new Params();
        }

        @Override
        public AST visitMoreParams(MoreParamsContext ctx) {
            Params params = (Params) visit(ctx.params());
            params.params.add(ctx.ID().getText());
            return params;
        }

        @Override
        public AST visitEndFuncs(EndFuncsContext ctx) {
            return new Funcs();
        }

        @Override
        public AST visitMoreFuncs(MoreFuncsContext ctx) {
            Funcs funcs = (Funcs) visit(ctx.funcs());
            funcs.funcs.add((Func) visit(ctx.func_def()));
            return funcs;
        }

        @Override
        public AST visitFunc_def(Func_defContext ctx) {
            ArrayList<String> params = ((Params) visit(ctx.params())).params;
            return new Func(ctx.ID().getText(), params.toArray(new String[0]), (V) visit(ctx.mealy_union()));
        }

        @Override
        public AST visitEpsilonProduct(EpsilonProductContext ctx) {
            return visit(ctx.mealy_atomic());
        }

        @Override
        public AST visitAtomicVarID(AtomicVarIDContext ctx) {
            return new WithVars.Var(ctx.ID().getText());
        }

        @Override
        public AST visitAtomicNested(AtomicNestedContext ctx) {
            return visit(ctx.mealy_union());
        }

        @Override
        public AST visitProduct(ProductContext ctx) {
            final String quotedLiteral = ctx.StringLiteral().getText();
            final String unquotedLiteral = quotedLiteral.substring(1, quotedLiteral.length() - 1);
            return new WithVars.Product((V) visit(ctx.mealy_atomic()), unquotedLiteral);
        }

        @Override
        public AST visitAtomicLiteral(AtomicLiteralContext ctx) {
            final String quotedLiteral = ctx.StringLiteral().getText();
            final String unquotedLiteral = quotedLiteral.substring(1, quotedLiteral.length() - 1);
            return new WithVars.Atomic(unquotedLiteral);
        }

        @Override
        public AST visitNoKleeneClosure(NoKleeneClosureContext ctx) {
            return visit(ctx.mealy_prod());
        }

        @Override
        public AST visitKleeneClosure(KleeneClosureContext ctx) {
            final TerminalNode w = ctx.Weight();
            V nested = (V) visit(ctx.mealy_prod());
            if(w==null) {
                return new WithVars.Kleene(nested);
            }else {
                final int i = Integer.parseInt(w.getText());
                return new WithVars.Kleene(new WithVars.WeightAfter(nested, i));
            }
        }

        @Override
        public AST visitEndConcat(EndConcatContext ctx) {
            final TerminalNode w = ctx.Weight();
            V lhs = (V) visit(ctx.mealy_Kleene_closure());
            if(w==null) {
                return lhs;
            }else {
                final int i = Integer.parseInt(w.getText());
                return new WithVars.WeightAfter(lhs, i);
            }
        }

        @Override
        public AST visitMoreConcat(MoreConcatContext ctx) {
            V lhs = (V) visit(ctx.mealy_Kleene_closure());
            V rhs = (V) visit(ctx.mealy_concat());
            
            final TerminalNode w = ctx.Weight();
            if(w==null) {
                return new WithVars.Concat(lhs,  rhs);
            }else {
                final int i = Integer.parseInt(w.getText());
                return new WithVars.Concat(new WithVars.WeightAfter(lhs, i), rhs) ;
            }
        }

        @Override
        public AST visitAtomicRange(AtomicRangeContext ctx) {
            final int[] range = ctx.Range().getText().codePoints().toArray();
            final int from, to;
            // [a-b] or [\a-b] or [a-\b] or [\a-\b]
            if (range[1] == '\\') {
                // [\a-b] or [\a-\b]
                from = escapeCharacter(range[2]);
                if (range[4] == '\\') {
                    // [\a-\b]
                    to = escapeCharacter(range[5]);
                } else {
                    // [\a-b]
                    to = range[4];
                }
            } else {
                // [a-b] or [a-\b]
                from = range[1];
                if (range[3] == '\\') {
                    // [a-\b]
                    to = escapeCharacter(range[4]);
                } else {
                    // [a-b]
                    to = range[3];
                }
            }
            return new WithVars.Range(from, to);
        }

        @Override
        public AST visitMoreUnion(MoreUnionContext ctx) {
            V lhs =(V) visit(ctx.mealy_concat());
            V rhs =(V) visit(ctx.mealy_union());
            
            final TerminalNode w = ctx.Weight();
            if(w==null) {
                return new WithVars.Union(lhs,  rhs);
            }else {
                final int i = Integer.parseInt(w.getText());
                return new WithVars.Union(new WithVars.WeightBefore(lhs, i), rhs) ;
            } 
        }

        @Override
        public AST visitEndUnion(EndUnionContext ctx) {
            V lhs = (V) visit(ctx.mealy_concat());
            final TerminalNode w = ctx.Weight();
            if(w==null) {
                return lhs;
            }else {
                final int i = Integer.parseInt(w.getText());
                return new WithVars.WeightBefore(lhs, i) ;
            } 
        }

        @Override
        public AST visitStart(StartContext ctx) {
            return visit(ctx.funcs());
        }
    }

    public static Funcs parse(String source) {
        return parse(CharStreams.fromString(source));
    }

    public static Funcs parse(CharStream source) {

        GrammarLexer lexer = new GrammarLexer(source);
        GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                    int charPositionInLine, String msg, RecognitionException e) {
                System.err.println("line " + line + ":" + charPositionInLine + " " + msg + " " + e);
            }
        });
        GrammarVisitor visitor = new GrammarVisitor();
        Funcs funcs = (Funcs) visitor.visit(parser.start());
        return funcs;

    }

    public static HashMap<String, A> eval(Funcs funcs) {
        HashMap<String, A> evaluated = new HashMap<>();
        for (Func f : funcs.funcs) {
            evaluated.put(f.name, f.body.substituteVars(evaluated));
        }
        return evaluated;
    }
}
