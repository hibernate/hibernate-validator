/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.scriptengine;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;


/**
 * Factory responsible for the creation of {@link ScriptEvaluator}s. This
 * class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ScriptEvaluatorFactory {

	/**
	 * A reference with an instance of this factory. Allows the factory to be reused several times, but can be GC'ed if required.
	 */
	private static Reference<ScriptEvaluatorFactory> INSTANCE = new SoftReference<ScriptEvaluatorFactory>( new ScriptEvaluatorFactory() );

	/**
	 * A cache of script executors (keyed by language name).
	 */
	private final ConcurrentMap<String, ScriptEvaluator> scriptExecutorCache = new ConcurrentHashMap<String, ScriptEvaluator>();

	private ScriptEvaluatorFactory() {
	}

	/**
	 * Retrieves an instance of this factory.
	 *
	 * @return A script evaluator factory. Never null.
	 */
	public static synchronized ScriptEvaluatorFactory getInstance() {
		ScriptEvaluatorFactory theValue = INSTANCE.get();

		if ( theValue == null ) {
			theValue = new ScriptEvaluatorFactory();
			INSTANCE = new SoftReference<ScriptEvaluatorFactory>( theValue );
		}

		return theValue;
	}

	/**
	 * Retrieves a script executor for the given language.
	 *
	 * @param languageName The name of a scripting language as expected by {@link ScriptEngineManager#getEngineByName(String)}.
	 *
	 * @return A script executor for the given language. Never null.
	 *
	 * @throws ScriptException In case no JSR 223 compatible engine for the given language could be found.
	 */
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) throws ScriptException {
		if ( !scriptExecutorCache.containsKey( languageName ) ) {

			ScriptEvaluator scriptExecutor = createNewScriptEvaluator( languageName );
			scriptExecutorCache.putIfAbsent( languageName, scriptExecutor );
		}

		return scriptExecutorCache.get( languageName );
	}

	/**
	 * Creates a new script executor for the given language.
	 *
	 * @param languageName A JSR 223 language name.
	 *
	 * @return A newly created script executor for the given language.
	 *
	 * @throws ScriptException In case no JSR 223 compatible engine for the given language could be found.
	 */
	private ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName( languageName );

		if ( engine == null ) {
			throw new ScriptException( MESSAGES.unableToFindScriptEngine( languageName ) );
		}

		return new ScriptEvaluator( engine );
	}
}
