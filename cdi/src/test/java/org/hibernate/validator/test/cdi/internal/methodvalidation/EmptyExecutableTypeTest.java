/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class EmptyExecutableTypeTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Snafu.class )
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	Snafu snafu;

	@Test
	public void testEmptyExecutableTypeParameterIsTreatedAsExecutableTypeNone() throws Exception {
		assertThat( snafu.foo() ).isNull();
	}
}
