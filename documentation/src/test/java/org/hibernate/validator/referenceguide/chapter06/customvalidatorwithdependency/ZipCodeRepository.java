package org.hibernate.validator.referenceguide.chapter06.customvalidatorwithdependency;

public interface ZipCodeRepository {
	boolean isExist(String zipCode);
}
