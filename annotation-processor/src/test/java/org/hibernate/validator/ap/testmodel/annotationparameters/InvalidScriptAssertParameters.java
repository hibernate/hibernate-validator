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
public class InvalidScriptAssertParameters {

	@ScriptAssert(lang = " ", script = "some script")
	public static class Case1 {
	}

	@ScriptAssert(lang = " ", script = "some script", alias = "some alias")
	public static class Case2 {
	}

	@ScriptAssert(lang = " ", script = "some script", alias = " ")
	public static class Case3 {
	}

	@ScriptAssert(lang = "some lang", script = " ")
	public static class Case4 {
	}

	@ScriptAssert(lang = "some lang", script = " ", alias = "some alias")
	public static class Case5 {
	}

	@ScriptAssert(lang = "some lang", script = " ", alias = " ")
	public static class Case6 {
	}

	@ScriptAssert(lang = "some lang", script = "some script", alias = " ")
	public static class Case7 {
	}

	@ScriptAssert(lang = " ", script = " ")
	public static class Case8 {
	}

	@ScriptAssert(lang = " ", script = " ", alias = "some alias")
	public static class Case9 {
	}

	@ScriptAssert(lang = " ", script = " ", alias = " ")
	public static class Case10 {
	}

	@ScriptAssert.List({ @ScriptAssert(lang = " ", script = " ", alias = " "), @ScriptAssert(lang = " ", script = " ", alias = "some alias") })
	public static class Case11 {
	}

}
