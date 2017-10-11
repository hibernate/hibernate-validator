/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.scripting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngineManager;

/**
 * Basic cacheable factory responsible for the creation of {@link ScriptEvaluator}s. This
 * class is thread-safe. Caches {@code ScriptEvaluator} when they are requested.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public abstract class AbstractCachingScriptEvaluatorFactory implements ScriptEvaluatorFactory {

	/**
	 * A cache of script evaluators (keyed by language name).
	 */
	private final ConcurrentMap<String, ScriptEvaluator> scriptEvaluatorCache = new ConcurrentHashMap<>();

	/**
	 * Retrieves a script executor for the given language.
	 *
	 * @param languageName The name of a scripting language as expected by {@link ScriptEngineManager#getEngineByName(String)}.
	 * @return A script executor for the given language. Never null.
	 *
	 * @throws ScriptEvaluatorNotFoundException In case no compatible evaluator for the given language could be found.
	 */
	@Override
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
		return scriptEvaluatorCache.computeIfAbsent( languageName, this::createNewScriptEvaluator );
	}

	/**
	 * Creates a new script evaluator for the given language.
	 *
	 * @param languageName A language name.
	 * @return A newly created script evaluator for the given language.
	 *
	 * @throws ScriptEvaluatorNotFoundException In case no compatible engine for the given language could be found.
	 */
	protected abstract ScriptEvaluator createNewScriptEvaluator(String languageName) throws ScriptEvaluatorNotFoundException;
}
