/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import org.hibernate.validator.constraints.ScriptAssert;

/**
 * @author Marko Bekhta
 */
public class ValidScriptAssertParameters {

	@ScriptAssert(lang = "some lang", script = "some script")
	public static class Case1 {

	}

	@ScriptAssert(lang = "some lang", script = "some script", alias = "some alias")
	public static class Case2 {

	}

	@ScriptAssert.List({
			@ScriptAssert(lang = "some lang", script = "some script", alias = "some alias"),
			@ScriptAssert(lang = "some lang", script = "some script")
	})
	public static class Case3 {

	}

}
