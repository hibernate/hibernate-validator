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
package org.hibernate.validator.util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.slf4j.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.engine.ValidatorConfiguration;
import org.hibernate.validator.engine.PathImpl;

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
			ValidatorConfiguration configuration = Validation
					.byProvider( HibernateValidator.class )
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

		ValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();
		return configuration.buildValidatorFactory().getValidator();
	}

	/**
	 * @return A <code>Validator</code> instance which ignores <i>validation.xml</code>.
	 */
	public static Validator getValidatorIgnoringValidationXml() {
		Thread.currentThread().setContextClassLoader( new IgnoringValidationXmlClassLoader() );

		ValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
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

	public static <T> void assertCorrectConstraintViolationMessages(Set<ConstraintViolation<T>> violations, String... messages) {
		List<String> actualMessages = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualMessages.add( violation.getMessage() );
		}

		assertTrue( actualMessages.size() == messages.length, "Wrong number or error messages" );

		for ( String expectedMessage : messages ) {
			assertTrue(
					actualMessages.contains( expectedMessage ),
					"The message '" + expectedMessage + "' should have been in the list of actual messages: " + actualMessages
			);
			actualMessages.remove( expectedMessage );
		}
		assertTrue(
				actualMessages.isEmpty(), "Actual messages contained more messages as specidied expected messages"
		);
	}

	public static <T> void assertCorrectConstraintTypes(Set<ConstraintViolation<T>> violations, Class<?>... expectedConsraintTypes) {
		List<String> actualConstraintTypes = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualConstraintTypes.add(
					( ( Annotation ) violation.getConstraintDescriptor().getAnnotation() ).annotationType().getName()
			);
		}

		assertEquals(
				expectedConsraintTypes.length, actualConstraintTypes.size(), "Wrong number of constraint types."
		);

		for ( Class<?> expectedConstraintType : expectedConsraintTypes ) {
			assertTrue(
					actualConstraintTypes.contains( expectedConstraintType.getName() ),
					"The constraint type " + expectedConstraintType.getName() + " should have been violated."
			);
		}
	}

	public static <T> void assertCorrectPropertyPaths(Set<ConstraintViolation<T>> violations, String... propertyPaths) {
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
				PathImpl.createPathFromString( propertyPath ),
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
