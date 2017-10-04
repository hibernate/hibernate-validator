//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.util.Map;

import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluatorFactory;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

//tag::include[]
public class SpringELEvaluatorFactory implements ScriptEvaluatorFactory {

	@Override
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
		if ( !"spring".equalsIgnoreCase( languageName ) ) {
			throw new IllegalStateException( "Only Spring EL is supported" );
		}
		return new ScriptEvaluator() {

			ExpressionParser expressionParser = new SpelExpressionParser();

			@Override
			public Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
				try {
					Expression expression = expressionParser.parseExpression( script );
					EvaluationContext context = new StandardEvaluationContext( bindings.values().iterator().next() );
					return expression.getValue( context );
				}
				catch (ParseException | EvaluationException e) {
					throw new ScriptEvaluationException( "Wasn't able to evaluate SPEL script", e );
				}
			}
		};
	}
}
//end::include[]
