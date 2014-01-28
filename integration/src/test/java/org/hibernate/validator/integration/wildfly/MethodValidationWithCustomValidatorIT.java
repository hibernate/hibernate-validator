/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.integration.wildfly;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Asserts that the validation interceptor picks up a {@code Validator} provided by the application and uses it for
 * validation.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
@Ignore
// TODO HV-837 Enable once WF 8.0.0.Final can be used for testing (requires WFLY-2762)
public class MethodValidationWithCustomValidatorIT {

	private static final String WAR_FILE_NAME = MethodValidationWithCustomValidatorIT.class
			.getSimpleName() + ".war";

	public static class MyService {

		public void doSomething(@NotNull String param) {
		}
	}

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( MyValidator.class )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private MyService myService;

	@Inject
	private MyValidator validator;

	@Test
	public void shouldUseApplicationProvidedValidatorForMethodValidation() {
		assertEquals( 0, validator.getForExecutablesInvocationCount() );
		myService.doSomething( "foobar" );
		assertEquals( "MyValidator#forExecutable() should have been invoked once.", 1, validator.getForExecutablesInvocationCount() );
	}
}
