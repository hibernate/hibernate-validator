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

	// Because asserj requires junit in its moduleinfo things become a mess:
	requires static org.junit.platform.commons;
	requires static org.junit.platform.launcher;

	opens org.hibernate.validator.integrationtest.java.module.test.utils to org.hibernate.validator, org.junit.platform.commons;

}
