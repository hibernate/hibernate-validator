/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.scripting.impl;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptEngineFactory;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl;
import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;

/**
 * Allows to pass a list of {@link ScriptEngineFactory} instances, which will be used to create {@link ScriptEvaluator}s.
 * Might be useful in cases where you want to control which exact {@link ScriptEngineFactory} is used to create
 * {@link ScriptEvaluator} (when there's multiple options present for a language of interest).
 *
 * @author Marko Bekhta
 */
public class DeclarativeScriptEvaluatorFactory extends AbstractCacheableScriptEvaluatorFactory {

	private final Map<String, ScriptEngineFactory> factoryMap;

	public DeclarativeScriptEvaluatorFactory(ScriptEngineFactory... factories) {
		this.factoryMap = Arrays.stream( factories )
				.collect(
						Collectors.collectingAndThen(
								Collectors.toMap( s -> s.getLanguageName().toLowerCase(), Function.identity() ),
								CollectionHelper::toImmutableMap
						)

				);
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptEvaluationException {
		ScriptEngineFactory factory = factoryMap.get( languageName.toLowerCase() );
		if ( factory == null ) {
			throw new ScriptEvaluationException( MESSAGES.unableToFindScriptEngine( languageName ) );
		}
		return new ScriptEvaluatorImpl( factory.getScriptEngine() );
	}

}
