/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
module org.hibernate.validator.integrationtest.java.module.test.utils {

	requires jakarta.validation;
	requires org.hibernate.validator;
	requires org.hibernate.validator.testutils;
	requires org.assertj.core;
	requires org.glassfish.expressly;

	opens org.hibernate.validator.integrationtest.java.module.test.utils to org.hibernate.validator;

}
