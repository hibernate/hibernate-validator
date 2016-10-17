/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import org.hibernate.validator.constraints.NumberPattern;

/**
 * @author Marko Bekhta
 */
public class InvalidNumberPatternParameters {

	@NumberPattern(regexp = "\\")
	private double double1;

	@NumberPattern(regexp = "[124-7]", numberFormat = "some.wrong.format")
	private double double2;

	@NumberPattern(regexp = "[*", numberFormat = "###.##.and.wrong.again")
	private double double3;

	@NumberPattern.List({
			@NumberPattern(regexp = "\\"),
			@NumberPattern(regexp = "[124-7]", numberFormat = "some.wrong.format"),
			@NumberPattern(regexp = "[*", numberFormat = "###.##.and.wrong.again")
	})
	private double double4;

}
