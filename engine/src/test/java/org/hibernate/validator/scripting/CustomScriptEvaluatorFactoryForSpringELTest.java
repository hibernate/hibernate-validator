/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.scripting;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Map;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.ScriptAssert;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class CustomScriptEvaluatorFactoryForSpringELTest {

	@Test
	public void testCreateNewScriptEvaluator() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.scriptEvaluatorFactory( new SpringELFactory() )
				.buildValidatorFactory()
				.getValidator();
		assertThat( validator.validate( new SpringELFactory.Foo( 1 ) ) ).isEmpty();

		assertThat( validator.validate( new SpringELFactory.Foo( -1 ) ) ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
		);
	}

	private static class SpringELFactory implements ScriptEvaluatorFactory {

		@Override
		public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
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

		@ScriptAssert(script = "value > 0", lang = "spring")
		private static class Foo {

			private final int value;

			private Foo(int value) {
				this.value = value;
			}

			public int getValue() {
				return value;
			}
		}

	}
}
