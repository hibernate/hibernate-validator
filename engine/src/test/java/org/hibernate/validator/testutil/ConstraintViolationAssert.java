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
package org.hibernate.validator.testutil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import org.fest.assertions.Assertions;
import org.fest.assertions.CollectionAssert;

import static org.fest.assertions.Formatting.format;
import static org.hibernate.validator.internal.engine.path.PathImpl.createPathFromString;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class provides useful functions to assert correctness of constraint violations raised
 * during tests.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class ConstraintViolationAssert {

	/**
	 * Expected name for cross-parameter nodes.
	 */
	private static final String CROSS_PARAMETER_NODE_NAME = "<cross-parameter>";

	/**
	 * Expected name for cross-parameter nodes.
	 */
	private static final String RETURN_VALUE_NODE_NAME = "<return value>";

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
	public static void assertCorrectConstraintViolationMessages(Set<? extends ConstraintViolation<?>> violations,
																String... expectedMessages) {
		List<String> actualMessages = newArrayList();
		for ( ConstraintViolation<?> violation : violations ) {
			actualMessages.add( violation.getMessage() );
		}

		Assertions.assertThat( actualMessages ).containsOnly( (Object[]) expectedMessages );
	}

	public static void assertCorrectConstraintViolationMessages(ConstraintViolationException e,
																String... expectedMessages) {
		assertCorrectConstraintViolationMessages( e.getConstraintViolations(), expectedMessages );
	}

	/**
	 * Asserts that the violated constraint type in the violation list matches exactly the expected constraint types
	 * list.
	 *
	 * @param violations The violation list to verify.
	 * @param expectedConstraintTypes The expected constraint types.
	 */
	public static <T> void assertCorrectConstraintTypes(Set<ConstraintViolation<T>> violations,
														Class<?>... expectedConstraintTypes) {
		List<Class<? extends Annotation>> actualConstraintTypes = newArrayList();
		for ( ConstraintViolation<?> violation : violations ) {
			actualConstraintTypes.add( violation.getConstraintDescriptor().getAnnotation().annotationType() );
		}

		assertCorrectConstraintTypes( actualConstraintTypes, expectedConstraintTypes );
	}

	/**
	 * Asserts that the given list of constraint violation paths matches the list of expected property paths.
	 *
	 * @param violations The violation list to verify.
	 * @param expectedPropertyPaths The expected property paths.
	 */
	public static void assertCorrectPropertyPaths(Set<? extends ConstraintViolation<?>> violations,
												  String... expectedPropertyPaths) {
		List<String> expectedPathsAsList = Arrays.asList( expectedPropertyPaths );

		List<String> actualPaths = newArrayList();
		for ( ConstraintViolation<?> violation : violations ) {
			actualPaths.add( violation.getPropertyPath().toString() );
		}

		Collections.sort( expectedPathsAsList );
		Collections.sort( actualPaths );

		assertEquals(
				actualPaths,
				expectedPathsAsList,
				String.format( "Expected %s, but got %s", expectedPathsAsList, actualPaths )
		);
	}

	public static ConstraintViolationSetAssert assertThat(Set<? extends ConstraintViolation<?>> actualViolations) {
		return new ConstraintViolationSetAssert( actualViolations );
	}

	public static void assertCorrectPropertyPaths(ConstraintViolationException e, String... expectedPropertyPaths) {
		assertCorrectPropertyPaths( e.getConstraintViolations(), expectedPropertyPaths );
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
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage,
												 Class<?> rootBeanClass, Object invalidValue, String propertyPath) {
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
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage,
												 Class<?> rootBeanClass, Object invalidValue) {
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
	public static void assertConstraintViolation(ConstraintViolation<?> violation, String errorMessage,
												 Class<?> rootBeanClass) {
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
	public static void assertNumberOfViolations(Set<? extends ConstraintViolation<?>> violations,
												int numberOfViolations) {
		assertEquals(
				violations.size(),
				numberOfViolations,
				"Wrong number of constraint violations"
		);
	}

	public static void assertConstraintTypes(Set<ConstraintDescriptor<?>> descriptors,
											 Class<?>... expectedConstraintTypes) {
		List<Class<? extends Annotation>> actualConstraintTypes = newArrayList();

		for ( ConstraintDescriptor<?> descriptor : descriptors ) {
			actualConstraintTypes.add( descriptor.getAnnotation().annotationType() );
		}

		assertCorrectConstraintTypes( actualConstraintTypes, expectedConstraintTypes );
	}

	/**
	 * Asserts that the nodes in the path have the specified kinds.
	 *
	 * @param path The path under test
	 * @param kinds The node kinds
	 */
	public static void assertNodeKinds(Path path, ElementKind... kinds) {
		Iterator<Path.Node> pathIterator = path.iterator();

		for ( ElementKind kind : kinds ) {
			assertTrue( pathIterator.hasNext() );
			assertEquals( pathIterator.next().getKind(), kind );
		}

		assertFalse( pathIterator.hasNext() );
	}

	/**
	 * Asserts that the nodes in the path have the specified names.
	 *
	 * @param path The path under test
	 * @param names The node names
	 */
	public static void assertNodeNames(Path path, String... names) {
		Iterator<Path.Node> pathIterator = path.iterator();

		for ( String name : names ) {
			assertTrue( pathIterator.hasNext() );
			assertEquals( pathIterator.next().getName(), name );
		}

		assertFalse( pathIterator.hasNext() );
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
	 * <p>
	 * Asserts that the two given collections contain the same constraint types.
	 * </p>
	 * <p>
	 * Multiset semantics is used for the comparison, i.e. the same constraint
	 * type can be contained several times in the compared collections, but the
	 * order doesn't matter. The comparison is done using the class names, since
	 * {@link Class} doesn't implement {@link Comparable}.
	 * </p>
	 *
	 * @param actualConstraintTypes The actual constraint types.
	 * @param expectedConstraintTypes The expected constraint types.
	 */
	private static <T> void assertCorrectConstraintTypes(Iterable<Class<? extends Annotation>> actualConstraintTypes,
														 Class<?>... expectedConstraintTypes) {
		List<String> expectedConstraintTypeNames = newArrayList();
		for ( Class<?> expectedConstraintType : expectedConstraintTypes ) {
			expectedConstraintTypeNames.add( expectedConstraintType.getName() );
		}

		List<String> actualConstraintTypeNames = newArrayList();
		for ( Class<?> actualConstraintType : actualConstraintTypes ) {
			actualConstraintTypeNames.add( actualConstraintType.getName() );
		}

		Collections.sort( expectedConstraintTypeNames );
		Collections.sort( actualConstraintTypeNames );

		assertEquals(
				actualConstraintTypeNames,
				expectedConstraintTypeNames,
				String.format( "Expected %s, but got %s", expectedConstraintTypeNames, actualConstraintTypeNames )
		);
	}

	public static PathExpectation pathWith() {
		return new PathExpectation();
	}

	public static class ConstraintViolationSetAssert extends CollectionAssert {

		private final Set<? extends ConstraintViolation<?>> actualViolations;

		protected ConstraintViolationSetAssert(Set<? extends ConstraintViolation<?>> actualViolations) {
			super( actualViolations );
			this.actualViolations = actualViolations;
		}

		public void containsOnlyPaths(PathExpectation... paths) {
			isNotNull();

			List<PathExpectation> expectedPaths = Arrays.asList( paths );
			List<PathExpectation> actualPaths = newArrayList();

			for ( ConstraintViolation<?> violation : actualViolations ) {
				actualPaths.add( new PathExpectation( violation.getPropertyPath() ) );
			}

			List<PathExpectation> actualPathsTmp = new ArrayList<PathExpectation>( actualPaths );
			actualPathsTmp.removeAll( expectedPaths );

			if ( !actualPathsTmp.isEmpty() ) {
				fail( format( "Found unexpected path(s): <%s>. Expected: <%s>", actualPathsTmp, expectedPaths ) );
			}

			List<PathExpectation> expectedPathsTmp = new ArrayList<PathExpectation>( expectedPaths );
			expectedPathsTmp.removeAll( actualPaths );

			if ( !expectedPathsTmp.isEmpty() ) {
				fail( format( "Missing expected path(s) <%s>. Actual paths: <%s>", expectedPathsTmp, actualPaths ) );
			}
		}

		public void containsPath(PathExpectation expectedPath) {
			isNotNull();

			List<PathExpectation> actualPaths = newArrayList();
			for ( ConstraintViolation<?> violation : actualViolations ) {
				PathExpectation actual = new PathExpectation( violation.getPropertyPath() );
				if ( actual.equals( expectedPath ) ) {
					return;
				}
				actualPaths.add( actual );
			}

			fail( format( "Didn't find path <%s> in actual paths <%s>.", expectedPath, actualPaths ) );
		}

		public void containsPaths(PathExpectation... expectedPaths) {
			for ( PathExpectation pathExpectation : expectedPaths ) {
				containsPath( pathExpectation );
			}
		}
	}

	/**
	 * A property path expected to be returned by a given {@link ConstraintViolation}.
	 */
	public static class PathExpectation {

		private final List<NodeExpectation> nodes = newArrayList();

		private PathExpectation() {
		}

		private PathExpectation(Path propertyPath) {
			for ( Path.Node node : propertyPath ) {
				Integer parameterIndex = null;
				if ( node.getKind() == ElementKind.PARAMETER ) {
					parameterIndex = node.as( Path.ParameterNode.class ).getParameterIndex();
				}
				nodes.add(
						new NodeExpectation(
								node.getName(),
								node.getKind(),
								node.isInIterable(),
								node.getKey(),
								node.getIndex(),
								parameterIndex
						)
				);
			}
		}

		public PathExpectation property(String name) {
			nodes.add( new NodeExpectation( name, ElementKind.PROPERTY ) );
			return this;
		}

		public PathExpectation property(String name, boolean inIterable, Object key, Integer index) {
			nodes.add( new NodeExpectation( name, ElementKind.PROPERTY, inIterable, key, index, null ) );
			return this;
		}

		public PathExpectation bean() {
			nodes.add( new NodeExpectation( null, ElementKind.BEAN ) );
			return this;
		}

		public PathExpectation bean(boolean inIterable, Object key, Integer index) {
			nodes.add( new NodeExpectation( null, ElementKind.BEAN, inIterable, key, index, null ) );
			return this;
		}

		public PathExpectation method(String name) {
			nodes.add( new NodeExpectation( name, ElementKind.METHOD ) );
			return this;
		}

		public PathExpectation parameter(String name, int index) {
			nodes.add( new NodeExpectation( name, ElementKind.PARAMETER, false, null, null, index ) );
			return this;
		}

		public PathExpectation crossParameter() {
			nodes.add( new NodeExpectation( CROSS_PARAMETER_NODE_NAME, ElementKind.CROSS_PARAMETER ) );
			return this;
		}

		public PathExpectation returnValue() {
			nodes.add( new NodeExpectation( RETURN_VALUE_NODE_NAME, ElementKind.RETURN_VALUE ) );
			return this;
		}

		@Override
		public String toString() {
			String lineBreak = System.getProperty( "line.separator" );
			StringBuilder asString = new StringBuilder( lineBreak + "PathExpectation(" + lineBreak );
			for ( NodeExpectation node : nodes ) {
				asString.append( "  " ).append( node ).append( lineBreak );
			}

			return asString.append( ")" ).toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( nodes == null ) ? 0 : nodes.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			PathExpectation other = (PathExpectation) obj;
			if ( nodes == null ) {
				if ( other.nodes != null ) {
					return false;
				}
			}
			else if ( !nodes.equals( other.nodes ) ) {
				return false;
			}
			return true;
		}
	}

	/**
	 * A node expected to be contained in the property path returned by a given {@link ConstraintViolation}.
	 */
	private static class NodeExpectation {
		private final String name;
		private final ElementKind kind;
		private final boolean inIterable;
		private final Object key;
		private final Integer index;
		private final Integer parameterIndex;

		private NodeExpectation(String name, ElementKind kind) {
			this( name, kind, false, null, null, null );
		}

		private NodeExpectation(String name, ElementKind kind, boolean inIterable, Object key, Integer index,
								Integer parameterIndex) {
			this.name = name;
			this.kind = kind;
			this.inIterable = inIterable;
			this.key = key;
			this.index = index;
			this.parameterIndex = parameterIndex;
		}

		@Override
		public String toString() {
			return "NodeExpectation(" + name + ", " + kind + ", " + inIterable
					+ ", " + key + ", " + index + ", " + parameterIndex + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( inIterable ? 1231 : 1237 );
			result = prime * result + ( ( index == null ) ? 0 : index.hashCode() );
			result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
			result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
			result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
			result = prime * result + ( ( parameterIndex == null ) ? 0 : parameterIndex.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			NodeExpectation other = (NodeExpectation) obj;
			if ( inIterable != other.inIterable ) {
				return false;
			}
			if ( index == null ) {
				if ( other.index != null ) {
					return false;
				}
			}
			else if ( !index.equals( other.index ) ) {
				return false;
			}
			if ( key == null ) {
				if ( other.key != null ) {
					return false;
				}
			}
			else if ( !key.equals( other.key ) ) {
				return false;
			}
			if ( kind != other.kind ) {
				return false;
			}
			if ( name == null ) {
				if ( other.name != null ) {
					return false;
				}
			}
			else if ( !name.equals( other.name ) ) {
				return false;
			}
			if ( parameterIndex == null ) {
				if ( other.parameterIndex != null ) {
					return false;
				}
			}
			else if ( !parameterIndex.equals( other.parameterIndex ) ) {
				return false;
			}
			return true;
		}
	}
}
