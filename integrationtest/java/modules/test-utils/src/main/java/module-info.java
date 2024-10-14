/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
module org.hibernate.validator.integrationtest.java.module.test.utils {

	requires jakarta.validation;
	requires org.hibernate.validator;
	requires org.hibernate.validator.testutils;
	requires org.assertj.core;
	requires org.glassfish.expressly;

	opens org.hibernate.validator.integrationtest.java.module.test.utils to org.hibernate.validator;

}
