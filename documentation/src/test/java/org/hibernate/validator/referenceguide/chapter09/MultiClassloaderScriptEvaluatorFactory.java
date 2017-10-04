//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl;
import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluatorFactory;

/**
 * {@link ScriptEvaluatorFactory} which allows you to pass multiple {@link ClassLoader}s that will be used
 * to search for {@link ScriptEngine}s. Is useful in environments similar to OSGi, where script engines can be
 * found only by {@link ClassLoader} different from default one.
 */
//tag::include[]
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
				return new ScriptEvaluatorImpl( engine );
			}
		}
		throw new ScriptEvaluationException( "No JSR 223 script engine found for language " + languageName );
	}
}
//end::include[]
