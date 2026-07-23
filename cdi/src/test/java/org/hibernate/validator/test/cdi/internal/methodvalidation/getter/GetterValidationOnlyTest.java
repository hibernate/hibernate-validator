/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

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
public class GetterValidationOnlyTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( OnlyGetterValidated.class )
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	OnlyGetterValidated onlyGetterValidated;

	@Test
	public void testNonGetterValidationDoesNotOccur() throws Exception {
		assertThat( onlyGetterValidated.foo() ).isNull();
	}

	@Test
	public void testGetterValidationOccurs() throws Exception {
		assertThatThrownBy( () -> onlyGetterValidated.getFoo() )
				.isInstanceOf( ConstraintViolationException.class );
	}
}
