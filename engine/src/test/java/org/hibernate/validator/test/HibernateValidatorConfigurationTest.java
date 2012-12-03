/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test;

import javax.validation.Validation;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import static org.testng.Assert.assertNotNull;

/**
 * Test for {@link org.hibernate.validator.HibernateValidatorConfiguration}.
 *
 * @author Gunnar Morling
 */
public class HibernateValidatorConfigurationTest {

	@Test
	public void defaultResourceBundleLocatorCanBeRetrieved() {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		ResourceBundleLocator defaultResourceBundleLocator = configure.getDefaultResourceBundleLocator();

		assertNotNull( defaultResourceBundleLocator );
	}
}
