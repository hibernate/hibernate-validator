// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.util;

import java.io.InputStream;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.slf4j.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.hibernate.validation.HibernateValidationProvider;
import org.hibernate.validation.engine.HibernateValidatorConfiguration;

/**
 * Tests for the <code>ReflectionHelper</code>.
 *
 * @author Hardy Ferentschik
 */
public class TestUtil {
	private static final Logger log = LoggerFactory.make();

	private static Validator hibernateValidator;

	private TestUtil() {
	}

	public static Validator getValidator() {
		if ( hibernateValidator == null ) {
			HibernateValidatorConfiguration configuration = Validation
					.byProvider( HibernateValidationProvider.class )
					.configure();
			hibernateValidator = configuration.buildValidatorFactory().getValidator();
		}
		return hibernateValidator;
	}

	/**
	 * @param path The path to the xml file which should server as <code>validation.xml</code> for the returned
	 * <code>Validator</code>.
	 *
	 * @return A <code>Validator</code> instance which respects the configuration specified in the file with the path
	 *         <code>path</code>.
	 */
	public static Validator getValidatorWithCustomConfiguration(String path) {
		Thread.currentThread().setContextClassLoader( new CustomValidationXmlClassLoader( path ) );

		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidationProvider.class )
				.configure();
		return configuration.buildValidatorFactory().getValidator();
	}

	/**
	 * @return A <code>Validator</code> instance which ignores <i>validation.xml</code>.
	 */
	public static Validator getValidatorIgnoringValidationXml() {
		Thread.currentThread().setContextClassLoader( new IgnoringValidationXmlClassLoader() );

		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidationProvider.class )
				.configure();
		return configuration.buildValidatorFactory().getValidator();
	}

	public static ConstraintDescriptor<?> getSingleConstraintDescriptorFor(Class<?> clazz, String property) {
		Set<ConstraintDescriptor<?>> constraintDescriptors = getConstraintDescriptorsFor( clazz, property );
		assertTrue(
				constraintDescriptors.size() == 1, "This method should only be used when there is a single constraint"
		);
		return constraintDescriptors.iterator().next();
	}

	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		Validator validator = getValidator();
		return validator.getConstraintsForClass( clazz ).getConstraintsForProperty( property );
	}

	public static Set<ConstraintDescriptor<?>> getConstraintDescriptorsFor(Class<?> clazz, String property) {
		ElementDescriptor elementDescriptor = getPropertyDescriptor( clazz, property );
		return elementDescriptor.getConstraintDescriptors();
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue, String propertyPath, Class leafBean) {
		assertEquals(

				violation.getLeafBean().getClass(),
				leafBean,
				"Wrong leaf bean type"
		);
		assertConstraintViolation( violation, errorMessage, rootBean, invalidValue, propertyPath );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue, String propertyPath) {
		assertEquals(
				violation.getPropertyPath(),
				propertyPath,
				"Wrong propertyPath"
		);
		assertConstraintViolation( violation, errorMessage, rootBean, invalidValue );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean, Object invalidValue) {
		assertEquals(
				violation.getInvalidValue(),
				invalidValue,
				"Wrong invalid value"
		);
		assertConstraintViolation( violation, errorMessage, rootBean );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String errorMessage, Class rootBean) {
		assertEquals(
				violation.getRootBean().getClass(),
				rootBean,
				"Wrong root bean type"
		);
		assertConstraintViolation( violation, errorMessage );
	}

	public static void assertConstraintViolation(ConstraintViolation violation, String message) {
		assertEquals( violation.getMessage(), message, "Wrong message" );
	}

	public static void assertNumberOfViolations(Set violations, int expectedViolations) {
		assertEquals( violations.size(), expectedViolations, "Wrong number of constraint violations" );
	}

	private static class CustomValidationXmlClassLoader extends ClassLoader {
		private final String customValidationXmlPath;

		CustomValidationXmlClassLoader(String pathToCustomValidationXml) {
			super( CustomValidationXmlClassLoader.class.getClassLoader() );
			customValidationXmlPath = pathToCustomValidationXml;
		}

		public InputStream getResourceAsStream(String path) {
			String finalPath = path;
			if ( "META-INF/validation.xml".equals( path ) ) {
				log.info( "Using {} as validation.xml", customValidationXmlPath );
				finalPath = customValidationXmlPath;
			}
			return super.getResourceAsStream( finalPath );
		}
	}

	private static class IgnoringValidationXmlClassLoader extends ClassLoader {
		IgnoringValidationXmlClassLoader() {
			super( IgnoringValidationXmlClassLoader.class.getClassLoader() );
		}

		public InputStream getResourceAsStream(String path) {
			if ( "META-INF/validation.xml".equals( path ) ) {
				log.info( "Ignoring call to load validation.xml" );
				return null;
			}
			return super.getResourceAsStream( path );
		}
	}
}
