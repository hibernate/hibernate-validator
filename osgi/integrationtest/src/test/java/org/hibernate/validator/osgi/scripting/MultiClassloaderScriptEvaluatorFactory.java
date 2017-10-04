/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.scripting;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluatorFactory;

/**
 * {@link ScriptEvaluatorFactory} which allows you to pass multiple {@link ClassLoader}s that will be used
 * to search for {@link ScriptEngine}s. Is useful in environments similar to OSGi, where script engines can be
 * found only by {@link ClassLoader} different from default one.
 *
 * @author Marko Bekhta
 */
public class MultiClassloaderScriptEvaluatorFactory implements ScriptEvaluatorFactory {

	/**
	 * A cache of script executors (keyed by language name).
	 */
	private final ConcurrentMap<String, ScriptEvaluator> scriptExecutorCache = new ConcurrentHashMap<>();

	private final ClassLoader[] classLoaders;

	public MultiClassloaderScriptEvaluatorFactory(ClassLoader... classLoaders) {
		if ( classLoaders.length == 0 ) {
			throw new IllegalArgumentException( "No class loaders were passed" );
		}
		this.classLoaders = classLoaders;
	}

	@Override
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
		return scriptExecutorCache.computeIfAbsent( languageName, this::createNewScriptEvaluator );
	}

	private ScriptEvaluator createNewScriptEvaluator(String languageName) {
		for ( ClassLoader classLoader : classLoaders ) {
			ScriptEngine engine = new ScriptEngineManager( classLoader ).getEngineByName( languageName );
			if ( engine != null ) {
				return (script, bindings) -> {
					try {
						return engine.eval( script, new SimpleBindings( bindings ) );
					}
					catch (ScriptException e) {
						throw new ScriptEvaluationException( e );
					}
				};
			}
		}
		throw new ScriptEvaluationException( MESSAGES.unableToFindScriptEngine( languageName ) );
	}

}
