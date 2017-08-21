/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.scriptengine.impl;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.hibernate.validator.cfg.scriptengine.ScriptEvaluator;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl;

/**
 * @author Marko Bekhta
 */
public class DeclarativeScriptEvaluatorFactory extends AbstractCacheableScriptEvaluatorFactory {

	private final Map<String, ScriptEngineFactory> factoryMap;

	public DeclarativeScriptEvaluatorFactory(ScriptEngineFactory... factories) {
		this.factoryMap = CollectionHelper.toImmutableMap(
				Arrays.stream( factories )
						.collect( Collectors.toMap( s -> s.getLanguageName().toLowerCase(), Function.identity() ) )
		);
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptException {
		ScriptEngineFactory factory = factoryMap.get( languageName.toLowerCase() );
		if ( factory == null ) {
			throw new ScriptException( MESSAGES.unableToFindScriptEngine( languageName ) );
		}
		return new ScriptEvaluatorImpl( factory.getScriptEngine() );
	}

}
