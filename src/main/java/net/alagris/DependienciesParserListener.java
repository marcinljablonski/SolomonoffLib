package net.alagris;

import java.util.*;
import java.util.regex.Pattern;

import net.alagris.GrammarParser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class DependienciesParserListener<Pipeline, Var, V, E, P, A, O extends Seq<A>, W, N, G extends IntermediateGraph<V, E, P, N>> implements GrammarListener {
    private final ParseSpecs<Pipeline, Var, V, E, P, A, O, W, N, G> specs;

    public DependienciesParserListener(ParseSpecs<Pipeline, Var, V, E, P, A, O, W, N, G> specs) {
        this.specs = specs;
    }

     public Var var(Pos pos, String id, boolean makeCopy) throws CompilationError {
         Var g = null;
         if (g == null) {
             throw new CompilationError.MissingFunction(pos, id);
         } else {
             return g;
         }
     }

    @Override
    public void exitFuncDef(FuncDefContext ctx) {
        final String funcName = ctx.ID().getText();
        final G funcBody = null;
    }

    @Override
    public void exitMealyAtomicVarID(MealyAtomicVarIDContext ctx) {
        try {

            final Var g = var(new Pos(ctx.start), ctx.ID().getText(), ctx.exponential!=null);
            //automata.push(specs.getGraph(g));
        } catch (CompilationError e) {
            throw new RuntimeException(e);
        }
    }

    public void parse(CharStream source) throws CompilationError {
        final GrammarLexer lexer = new GrammarLexer(source);
        final GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                System.err.println("line " + line + ":" + charPositionInLine + " " + msg + " " + e);
            }
        });
        try {
            ParseTreeWalker.DEFAULT.walk(this, parser.start());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof CompilationError) {
                throw (CompilationError) e.getCause();
            } else {
                throw e;
            }
        }

    }

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterStart(StartContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitStart(StartContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEndFuncs(EndFuncsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEndFuncs(EndFuncsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFuncDef(FuncDefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterHoarePipeline(HoarePipelineContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitHoarePipeline(HoarePipelineContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTypeJudgement(TypeJudgementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTypeJudgement(TypeJudgementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPipelineMealy(PipelineMealyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPipelineMealy(PipelineMealyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPipelineExternal(PipelineExternalContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPipelineExternal(PipelineExternalContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPipelineNested(PipelineNestedContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPipelineNested(PipelineNestedContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPipelineBegin(PipelineBeginContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPipelineBegin(PipelineBeginContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyUnion(MealyUnionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyUnion(MealyUnionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyEndConcat(MealyEndConcatContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyEndConcat(MealyEndConcatContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyMoreConcat(MealyMoreConcatContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyMoreConcat(MealyMoreConcatContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyKleeneClosure(MealyKleeneClosureContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyKleeneClosure(MealyKleeneClosureContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyNoKleeneClosure(MealyNoKleeneClosureContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyNoKleeneClosure(MealyNoKleeneClosureContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyProduct(MealyProductContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyProduct(MealyProductContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyProductCodepoints(MealyProductCodepointsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyProductCodepoints(MealyProductCodepointsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyEpsilonProduct(MealyEpsilonProductContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyEpsilonProduct(MealyEpsilonProductContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicLiteral(MealyAtomicLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicLiteral(MealyAtomicLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicRange(MealyAtomicRangeContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicRange(MealyAtomicRangeContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicCodepoint(MealyAtomicCodepointContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicCodepoint(MealyAtomicCodepointContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicVarID(MealyAtomicVarIDContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicExternal(MealyAtomicExternalContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicExternal(MealyAtomicExternalContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicNested(MealyAtomicNestedContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicNested(MealyAtomicNestedContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMealyAtomicExternalOperation(MealyAtomicExternalOperationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMealyAtomicExternalOperation(MealyAtomicExternalOperationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterInformant(InformantContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitInformant(InformantContext ctx) {
		// TODO Auto-generated method stub
		
	}
}
