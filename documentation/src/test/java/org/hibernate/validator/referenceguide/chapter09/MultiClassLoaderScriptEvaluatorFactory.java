//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.validator.scripting.AbstractCachingScriptEvaluatorFactory;
import org.hibernate.validator.scripting.ScriptEngineScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluatorFactory;

/**
 * {@link ScriptEvaluatorFactory} that allows you to pass multiple {@link ClassLoader}s that will be used
 * to search for {@link ScriptEngine}s. Useful in environments similar to OSGi, where script engines can be
 * found only in {@link ClassLoader}s different from default one.
 */
//tag::include[]
public class MultiClassLoaderScriptEvaluatorFactory extends AbstractCachingScriptEvaluatorFactory {

	private final ClassLoader[] classLoaders;

	public MultiClassLoaderScriptEvaluatorFactory(ClassLoader... classLoaders) {
		if ( classLoaders.length == 0 ) {
			throw new IllegalArgumentException( "No class loaders were passed" );
		}
		this.classLoaders = classLoaders;
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(String languageName) {
		for ( ClassLoader classLoader : classLoaders ) {
			ScriptEngine engine = new ScriptEngineManager( classLoader ).getEngineByName( languageName );
			if ( engine != null ) {
				return new ScriptEngineScriptEvaluator( engine );
			}
		}
		throw new ScriptEvaluationException( "No JSR 223 script engine found for language " + languageName );
	}
}
//end::include[]
