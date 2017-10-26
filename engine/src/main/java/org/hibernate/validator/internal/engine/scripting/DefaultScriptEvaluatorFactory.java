/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.scripting;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.spi.scripting.AbstractCachingScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEngineScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;

/**
 * Factory responsible for the creation of JSR 223 based {@link ScriptEngineScriptEvaluator}s. This
 * class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class DefaultScriptEvaluatorFactory extends AbstractCachingScriptEvaluatorFactory {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private ClassLoader classLoader;

	private volatile ScriptEngineManager scriptEngineManager;

	private volatile ScriptEngineManager threadContextClassLoaderScriptEngineManager;

	public DefaultScriptEvaluatorFactory(ClassLoader externalClassLoader) {
		classLoader = externalClassLoader == null ? DefaultScriptEvaluatorFactory.class.getClassLoader() : externalClassLoader;
	}

	@Override
	public void clear() {
		super.clear();

		classLoader = null;
		scriptEngineManager = null;
		threadContextClassLoaderScriptEngineManager = null;
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptEvaluationException {
		ScriptEngine engine = getScriptEngineManager().getEngineByName( languageName );

		// fall back to the TCCL
		if ( engine == null ) {
			engine = getThreadContextClassLoaderScriptEngineManager().getEngineByName( languageName );
		}

		if ( engine == null ) {
			throw LOG.getUnableToFindScriptEngineException( languageName );
		}

		return new ScriptEngineScriptEvaluator( engine );
	}

	private ScriptEngineManager getScriptEngineManager() {
		if ( scriptEngineManager == null ) {
			synchronized ( this ) {
				if ( scriptEngineManager == null ) {
					scriptEngineManager = new ScriptEngineManager( classLoader );
				}
			}
		}
		return scriptEngineManager;
	}

	private ScriptEngineManager getThreadContextClassLoaderScriptEngineManager() {
		if ( threadContextClassLoaderScriptEngineManager == null ) {
			synchronized ( this ) {
				if ( threadContextClassLoaderScriptEngineManager == null ) {
					threadContextClassLoaderScriptEngineManager = new ScriptEngineManager( run( GetClassLoader.fromContext() ) );
				}
			}
		}
		return threadContextClassLoaderScriptEngineManager;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
