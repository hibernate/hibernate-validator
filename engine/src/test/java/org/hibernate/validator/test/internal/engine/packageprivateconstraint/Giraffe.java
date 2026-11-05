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
