/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.testutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

import static org.hibernate.validator.engine.PathImpl.createPathFromString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * This class provides useful functions to assert correctness of constraint violations raised
 * during tests.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class ConstraintViolationAssert {
	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ConstraintViolationAssert() {
	}

	/**
	 * Asserts that the messages in the violation list matches exactly the expected messages list.
	 *
	 * @param violations The violation list to verify.
	 * @param expectedMessages The expected constraint violation messages.
	 */
	public static void assertCorrectConstraintViolationMessages(Set<? extends ConstraintViolation<?>> violations, String... expectedMessages) {
		final List<String> actualMessages = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualMessages.add( violation.getMessage() );
		}

		assertEquals( actualMessages.size(), expectedMessages.length, "Wrong number of error messages" );

		for ( String expectedMessage : expectedMessages ) {
			assertTrue(
					actualMessages.contains( expectedMessage ),
					"The message '" + expectedMessage + "' should have been in the list of actual messages: " + actualMessages
			);
		}
	}

	/**
	 * Asserts that the violated constraint type in the violation list matches exactly the expected constraint types
	 * list.
	 *
	 * @param violations The violation list to verify.
	 * @param expectedConstraintTypes The expected constraint types.
	 */
	public static void assertCorrectConstraintTypes(Set<? extends ConstraintViolation<?>> violations, Class<?>... expectedConstraintTypes) {
		final List<String> actualConstraintTypes = new ArrayList<String>();
		for ( ConstraintViolation<?> violation : violations ) {
			actualConstraintTypes.add(
					( violation.getConstraintDescriptor().getAnnotation() ).annotationType().getName()
			);
		}

		assertEquals(
				actualConstraintTypes.size(), expectedConstraintTypes.length, "Wrong number of constraint types"
		);

		for ( Class<?> expectedConstraintType : expectedConstraintTypes ) {
			assertTrue(
					actualConstraintTypes.contains( expectedConstraintType.getName() ),
					"The constraint type " + expectedConstraintType.getName() + " should have been violated"
			);
		}
	}

	/**
	 * Asserts that the given list of constraint violation paths matches the list of expected property paths.
	 *
	 * @param violations The violation list to verify.
	 * @param expectedPropertyPaths The expected property paths.
	 */
	public static void assertCorrectPropertyPaths(Set<? extends ConstraintViolation<?>> violations, String... expectedPropertyPaths) {
		final List<Path> propertyPathsOfViolations = new ArrayList<Path>();
		for ( ConstraintViolation<?> violation : violations ) {
			propertyPathsOfViolations.add( violation.getPropertyPath() );
		}

		assertEquals(
				propertyPathsOfViolations.size(), expectedPropertyPaths.length, "Wrong number of property paths"
		);

		for ( String propertyPath : expectedPropertyPaths ) {
			Path expectedPath = createPathFromString( propertyPath );
			boolean containsPath = false;
			for ( Path actualPath : propertyPathsOfViolations ) {
				if ( pathsAreEqual( expectedPath, actualPath ) ) {
					containsPath = true;
					break;
				}
			}
			if ( !containsPath ) {
				fail( expectedPath + " is not in the list of path instances contained in the actual constraint violations [" + propertyPathsOfViolations + "]" );
			}
		}
	}

	/**
	 * Asserts that the error message, root bean class, invalid value and property path of the given violation are equal
	 * to the expected message, root bean class, invalid value and propertyPath.
	 *
	 * @param violation The violation to verify.
	 * @param errorMessage The expected violation error message.
	 * @param rootBeanClass The expected root bean class.
	 * @param invalidValue The expected invalid value.
	 * @param propertyPath The expected property path.
	 */
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass, Object invalidValue, String propertyPath) {
		assertTrue(
				pathsAreEqual( violation.getPropertyPath(), createPathFromString( propertyPath ) ),
				"Wrong propertyPath"
		);
		assertConstraintViolation( violation, errorMessage, rootBeanClass, invalidValue );
	}

	/**
	 * Asserts that the error message, root bean class and invalid value of the given violation are equal to the
	 * expected message, root bean class and invalid value.
	 *
	 * @param violation The violation to verify.
	 * @param errorMessage The expected error message.
	 * @param rootBeanClass The expected root bean class.
	 * @param invalidValue The expected invalid value.
	 */
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass, Object invalidValue) {
		assertEquals( violation.getInvalidValue(), invalidValue, "Wrong invalid value" );
		assertConstraintViolation( violation, errorMessage, rootBeanClass );
	}

	/**
	 * Asserts that the error message and the root bean class of the given violation are equal to the expected message
	 * and root bean class.
	 *
	 * @param violation The violation to verify.
	 * @param errorMessage The expected error message.
	 * @param rootBeanClass The expected root bean class.
	 */
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage, Class<?> rootBeanClass) {
		assertEquals( violation.getRootBean().getClass(), rootBeanClass, "Wrong root bean type" );
		assertConstraintViolation( violation, errorMessage );
	}

	/**
	 * Asserts that the error message of the given violation is equal to the expected message.
	 *
	 * @param violation The violation to verify.
	 * @param errorMessage The expected error message.
	 */
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage) {
		assertEquals( violation.getMessage(), errorMessage, "Wrong expectedMessage" );
	}

	/**
	 * Asserts that the given violation list has the expected number of violations.
	 *
	 * @param violations The violation list to verify.
	 * @param numberOfViolations The expected number of violation.
	 */
	public static void assertNumberOfViolations(Set<? extends ConstraintViolation<?>> violations, int numberOfViolations) {
		assertEquals( violations.size(), numberOfViolations, "Wrong number of constraint violations" );
	}

	/**
	 * Checks that two property paths are equal.
	 *
	 * @param p1 The first property path.
	 * @param p2 The second property path.
	 *
	 * @return {@code true} if the given paths are equal, {@code false} otherwise.
	 */
	public static boolean pathsAreEqual(Path p1, Path p2) {
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

	/**
	 * Asserts that the given iterable has the expected size.
	 *
	 * @param iterable The iterable which size have to be verified.
	 * @param size The expected size.
	 */
	public static void assertIterableSize(Iterable<?> iterable, int size) {
		int i = 0;

		//noinspection UnusedDeclaration
		for ( @SuppressWarnings("unused") Object o : iterable ) {
			i++;
		}

		assertEquals( i, size, "Actual size of iterable [" + iterable + "] differed from expected size" );
	}
}
