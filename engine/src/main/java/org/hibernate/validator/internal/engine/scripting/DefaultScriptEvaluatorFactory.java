/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.scripting.AbstractCachingScriptEvaluatorFactory;
import org.hibernate.validator.scripting.ScriptEngineScriptEvaluator;
import org.hibernate.validator.scripting.ScriptEvaluationException;
import org.hibernate.validator.scripting.ScriptEvaluator;

/**
 * Factory responsible for the creation of JSR 223 based {@link ScriptEngineScriptEvaluator}s. This
 * class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public class DefaultScriptEvaluatorFactory extends AbstractCachingScriptEvaluatorFactory {

	private static final Log LOG = LoggerFactory.make();

	private final ClassLoader externalClassLoader;

	public DefaultScriptEvaluatorFactory(ClassLoader externalClassLoader) {
		this.externalClassLoader = externalClassLoader;
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptEvaluationException {
		ScriptEngine engine = getScriptEngineManager().getEngineByName( languageName );

		if ( engine == null ) {
			throw LOG.getUnableToFindScriptEngineException( languageName );
		}

		return new ScriptEngineScriptEvaluator( engine );
	}

	private ScriptEngineManager getScriptEngineManager() {
		return new ScriptEngineManager( externalClassLoader == null ? Thread.currentThread().getContextClassLoader() : externalClassLoader );
	}
}
