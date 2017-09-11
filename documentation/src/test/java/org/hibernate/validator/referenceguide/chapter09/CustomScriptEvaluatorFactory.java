package org.hibernate.validator.referenceguide.chapter09;

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
