package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;

public class ZipCodeCatalog {
	public ZipCodeCountryCatalog country(String s) {
		return new ZipCodeCountryCatalog();
	}
}
