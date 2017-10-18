//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.validator.spi.scripting.AbstractCachingScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

//tag::include[]
public class SpringELScriptEvaluatorFactory extends AbstractCachingScriptEvaluatorFactory {

	@Override
	public ScriptEvaluator createNewScriptEvaluator(String languageName) {
		if ( !"spring".equalsIgnoreCase( languageName ) ) {
			throw new IllegalStateException( "Only Spring EL is supported" );
		}

		return new SpringELScriptEvaluator();
	}

	private static class SpringELScriptEvaluator implements ScriptEvaluator {

		private final ExpressionParser expressionParser = new SpelExpressionParser();

		@Override
		public Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
			try {
				Expression expression = expressionParser.parseExpression( script );
				EvaluationContext context = new StandardEvaluationContext( bindings.values().iterator().next() );
				for ( Entry<String, Object> binding : bindings.entrySet() ) {
					context.setVariable( binding.getKey(), binding.getValue() );
				}
				return expression.getValue( context );
			}
			catch (ParseException | EvaluationException e) {
				throw new ScriptEvaluationException( "Unable to evaluate SpEL script", e );
			}
		}
	}
}
//end::include[]
