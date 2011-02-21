/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.spi.ValidationProvider;

import org.slf4j.Logger;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.engine.PathImpl;
import org.hibernate.validator.method.MethodValidator;
import org.hibernate.validator.util.LoggerFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * A helper providing useful functions for testing Hibernate Validator, e.g. for the assertion of constraint
 * violations.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class TestUtil {

	private static final Logger log = LoggerFactory.make();

	private static Validator hibernateValidator;

	private TestUtil() {
	}

	public static Validator getValidator() {
		if ( hibernateValidator == null ) {
			Configuration<HibernateValidatorConfiguration> configuration = getConfiguration( Locale.ENGLISH );
			configuration.traversableResolver( new DummyTraversableResolver() );
			hibernateValidator = configuration.buildValidatorFactory().getValidator();
		}
		return hibernateValidator;
	}

	public static MethodValidator getMethodValidator() {
		return getValidator().unwrap( MethodValidator.class );
	}

	public static Configuration<HibernateValidatorConfiguration> getConfiguration() {
		return getConfiguration( HibernateValidator.class, Locale.ENGLISH );
	}

	public static Configuration<HibernateValidatorConfiguration> getConfiguration(Locale locale) {
		return getConfiguration( HibernateValidator.class, locale );
	}

	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type) {
		return getConfiguration( type, Locale.ENGLISH );
	}

	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type, Locale locale) {
		Locale.setDefault( locale );
		return Validation.byProvider( type ).configure();
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
		return getConfiguration().buildValidatorFactory().getValidator();
	}

	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		Validator validator = getValidator();
		return validator.getConstraintsForClass( clazz ).getConstraintsForProperty( property );
	}

	public static Object getMethodValidationProxy(ValidationInvocationHandler handler) {
		final Class<?> wrappedObjectClass = handler.getWrapped().getClass();

		return Proxy.newProxyInstance(
				wrappedObjectClass.getClassLoader(),
				wrappedObjectClass.getInterfaces(),
				handler
		);
	}

	public static void assertCorrectConstraintViolationMessages(Set<? extends ConstraintViolation<?>> violations, String... messages) {
		List<String> actualMessages = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualMessages.add( violation.getMessage() );
		}

		assertEquals( actualMessages.size(), messages.length, "Wrong number of error messages" );

		for ( String expectedMessage : messages ) {
			assertTrue(
					actualMessages.contains( expectedMessage ),
					"The message '" + expectedMessage + "' should have been in the list of actual messages: " + actualMessages
			);
			actualMessages.remove( expectedMessage );
		}
		assertTrue(
				actualMessages.isEmpty(), "Actual messages contained more messages as specified expected messages"
		);
	}

	public static <T> void assertCorrectConstraintTypes(Set<ConstraintViolation<T>> violations, Class<?>... expectedConstraintTypes) {
		List<String> actualConstraintTypes = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualConstraintTypes.add(
					( (Annotation) violation.getConstraintDescriptor().getAnnotation() ).annotationType().getName()
			);
		}

		assertEquals(
				expectedConstraintTypes.length, actualConstraintTypes.size(), "Wrong number of constraint types."
		);

		for ( Class<?> expectedConstraintType : expectedConstraintTypes ) {
			assertTrue(
					actualConstraintTypes.contains( expectedConstraintType.getName() ),
					"The constraint type " + expectedConstraintType.getName() + " should have been violated."
			);
		}
	}

	public static void assertCorrectPropertyPaths(Set<? extends ConstraintViolation<?>> violations, String... propertyPaths) {
		List<Path> propertyPathsOfViolations = new ArrayList<Path>();
		for ( ConstraintViolation<?> violation : violations ) {
			propertyPathsOfViolations.add( violation.getPropertyPath() );
		}

		for ( String propertyPath : propertyPaths ) {
			Path expectedPath = PathImpl.createPathFromString( propertyPath );
			boolean containsPath = false;
			for ( Path actualPath : propertyPathsOfViolations ) {
				if ( assertEqualPaths( expectedPath, actualPath ) ) {
					containsPath = true;
					break;
				}
			}
			if ( !containsPath ) {
				fail( expectedPath + " is not in the list of path instances contained in the actual constraint violations: " + propertyPathsOfViolations );
			}
		}
	}

	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass, Object invalidValue, String propertyPath) {
		assertEquals(
				violation.getPropertyPath(),
				PathImpl.createPathFromString( propertyPath ),
				"Wrong propertyPath"
		);
		assertConstraintViolation( violation, errorMessage, rootBeanClass, invalidValue );
	}

	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass, Object invalidValue) {
		assertEquals(
				violation.getInvalidValue(),
				invalidValue,
				"Wrong invalid value"
		);
		assertConstraintViolation( violation, errorMessage, rootBeanClass );
	}

	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass) {
		assertEquals(
				violation.getRootBean().getClass(),
				rootBeanClass,
				"Wrong root bean type"
		);
		assertConstraintViolation( violation, errorMessage );
	}

	public static void assertConstraintViolation(ConstraintViolation<?> violation, String message) {
		assertEquals( violation.getMessage(), message, "Wrong message" );
	}

	public static void assertNumberOfViolations(Set<? extends ConstraintViolation<?>> violations, int expectedViolations) {
		assertEquals( violations.size(), expectedViolations, "Wrong number of constraint violations" );
	}

	public static boolean assertEqualPaths(Path p1, Path p2) {
		Iterator<Path.Node> p1Iterator = p1.iterator();
		Iterator<Path.Node> p2Iterator = p2.iterator();
		while ( p1Iterator.hasNext() ) {
			Path.Node p1Node = p1Iterator.next();
			if ( !p2Iterator.hasNext() ) {
				return false;
			}
			Path.Node p2Node = p2Iterator.next();

			// do the comparison on the node values
			if ( p2Node.getName() == null ) {
				if ( p1Node.getName() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getName().equals( p1Node.getName() ) ) {
				return false;
			}

			if ( p2Node.isInIterable() != p1Node.isInIterable() ) {
				return false;
			}


			if ( p2Node.getIndex() == null ) {
				if ( p1Node.getIndex() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getIndex().equals( p1Node.getIndex() ) ) {
				return false;
			}

			if ( p2Node.getKey() == null ) {
				if ( p1Node.getKey() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getKey().equals( p1Node.getKey() ) ) {
				return false;
			}
		}

		return !p2Iterator.hasNext();
	}

	public static void assertIterableSize(Iterable<?> iterable, int expectedCount) {
		int i = 0;

		for ( @SuppressWarnings("unused") Object o : iterable ) {
			i++;
		}

		assertEquals( i, expectedCount, "Actual size of iterable [" + iterable + "] differed from expected size." );
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
}
