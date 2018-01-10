package org.hibernate.validator.referenceguide.chapter09;

import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class CustomScriptEvaluatorFactory implements ScriptEvaluatorFactory {

	@Override
	public ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName) {
		return (script, bindings) -> true;
	}

	@Override
	public void clear() {
		// Nothing to do
	}
}
