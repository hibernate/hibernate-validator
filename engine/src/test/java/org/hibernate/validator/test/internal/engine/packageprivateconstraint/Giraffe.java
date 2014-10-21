/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.packageprivateconstraint;

/**
 * @author Gunnar Morling
 */
public class Giraffe {

	@ValidAnimalName("Bruno")
	public String getName() {
		return "Bob";
	}
}
