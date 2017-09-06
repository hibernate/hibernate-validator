/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import org.hibernate.validator.cfg.scriptengine.ScriptEvaluator;
import org.hibernate.validator.cfg.scriptengine.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class CustomScriptEvaluatorFactory implements ScriptEvaluatorFactory {

	@Override
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
		return (script, bindings) -> true;
	}
}
