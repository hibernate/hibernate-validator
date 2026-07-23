/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Hardy Ferentschik
 */
@ExtendWith(ArquillianExtension.class)
public class EmptyExecutableTypeTest {

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
