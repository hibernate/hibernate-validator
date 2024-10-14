/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
