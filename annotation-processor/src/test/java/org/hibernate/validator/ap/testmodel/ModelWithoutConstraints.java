package org.hibernate.validator.ap.testmodel;

public class ModelWithoutConstraints {

	@SomeAnnotation
	public String string;

	private @interface SomeAnnotation {
	}
}
