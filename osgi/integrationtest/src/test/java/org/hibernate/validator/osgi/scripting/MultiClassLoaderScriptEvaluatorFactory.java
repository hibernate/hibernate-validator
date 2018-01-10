/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.validator.spi.scripting.AbstractCachingScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEngineScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * {@link ScriptEvaluatorFactory} that allows you to pass multiple {@link ClassLoader}s that will be used
 * to search for {@link ScriptEngine}s. Useful in environments similar to OSGi, where script engines can be
 * found only in {@link ClassLoader}s different from default one.
 *
 * @author Marko Bekhta
 */
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
