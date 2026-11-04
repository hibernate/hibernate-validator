/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import org.hibernate.validator.test.util.TestHelper;

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
public class EnableGetterValidationInXmlTest {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Foo.class )
				.addClass( FooImpl.class )
				.addAsResource(
						TestHelper.getTestPackagePath( EnableGetterValidationInXmlTest.class ) + "validation-validate-executable-getter.xml",
						"META-INF/validation.xml"
				)
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	Foo foo;

	@Test
	public void testGetterValidationOccursBecauseItIsEnabledInXml() throws Exception {
		assertThatThrownBy( () -> foo.getFoo() )
				.isInstanceOf( ConstraintViolationException.class );
	}
}
