/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.valueextraction.model.CustomContainer;
import org.hibernate.validator.test.internal.engine.valueextraction.model.CustomContainerValueExtractor;
import org.hibernate.validator.test.internal.engine.valueextraction.model.ImprovedCustomContainer;
import org.hibernate.validator.test.internal.engine.valueextraction.model.ImprovedCustomContainerImpl;
import org.hibernate.validator.test.internal.engine.valueextraction.model.ImprovedCustomContainerValueExtractor;
import org.hibernate.validator.testutils.CandidateForTck;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@CandidateForTck
public class MultipleContainersAtTheSameTimeTest {

	private Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.addValueExtractor( new ImprovedCustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	/**
	 * Even though there runtime type implements both {@link List} and {@link CustomContainer} the constraint
	 * is declared only on the list side of the hierarchy hence we cannot bind this constraint to
	 * {@link CustomContainer} and as a result use {@code CustomContainerValueExtractor}
	 */
	@Test
	public void testMultipleContainersAtTheSameTimeShouldNotThrowException() throws Exception {
		List<String> strings = new Container();
		strings.add( "" );
		strings.add( null );

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( strings ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	/**
	 * Even though there runtime type implements both {@link List} and {@link CustomContainer} the constraint
	 * is declared only on the list side of the hierarchy hence we cannot bind this constraint to
	 * {@link CustomContainer} and as a result use {@code CustomContainerValueExtractor}
	 */
	@Test
	public void testMultipleContainersAtTheSameTimeShouldAlsoNotThrowException() throws Exception {
		BarContainer container = new BarContainer();
		container.add( null );
		container.add( "test" );
		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( new Bar( container ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	/**
	 * Test should fail as constraint is declared on a {@link FooBarContainer} which accept both
	 * {@code ListValueExtractor} as well as {@code CustomContainerValueExtractor}
	 */
	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void testMultipleContainersAtTheSameTimeShouldThrowException() throws Exception {
		validator.validate( new FooBar( new FooBarContainer<String>().add( "" ) ) );
	}

	/**
	 * Test should fail as constraint is declared on a {@link FooBarContainer} which accept both
	 * {@code ListValueExtractor} as well as {@code CustomContainerValueExtractor}
	 */
	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void testMultipleContainersAtTheSameTimeWithTypeParameterSpecificShouldThrowException() throws Exception {
		validator.validate( new FooBar( new FooBarContainer<String>().add( "" ) ) );
	}

	/**
	 * Test that container is determined at runtime and that maximally specific VE is used for that container.
	 */
	@Test
	public void testCascadingWhenUsingObjectReferenceUsesMostSpecificValueExtractor() throws Exception {
		class Bar {
			@Valid
			private final Object container;

			Bar(ImprovedCustomContainer<List<String>, String> container) {
				this.container = container;
			}
		}

		assertThatThrownBy( () -> validator.validate( new Bar( new ImprovedCustomContainerImpl<>( "" ) ) ) )
				.isInstanceOf( ValidationException.class )
				.hasCauseInstanceOf( IllegalStateException.class )
				.hasStackTraceContaining( "this extractor shouldn't be selected" );
	}

	/**
	 * Test for selecting a correct VE. Even though {@link ImprovedCustomContainerValueExtractor} is more
	 * specific by container type - it shouldn't be selected based on the type argument where the constraint is  placed.
	 * If {@link ImprovedCustomContainerValueExtractor} is selected an {@link IllegalStateException} will be thrown
	 * by this extractor and test will fail.
	 */
	@Test
	public void testFindingMaximallySpecificExtractorByTypeParameter() throws Exception {
		class Foo {
			@Valid
			private final ImprovedCustomContainer<@Valid List<@NotNull String>, String> container;

			Foo(ImprovedCustomContainer<List<String>, String> container) {
				this.container = container;
			}
		}

		ImprovedCustomContainerImpl<List<String>, String> container = new ImprovedCustomContainerImpl<>( "" );
		container.add( null );
		container.add( Collections.singletonList( "" ) );
		container.add( Collections.singletonList( null ) );
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( container ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);
	}

	private static class Foo {

		@Valid
		private final List<@NotNull String> notReallyJustAList;

		private Foo(List<String> notReallyJustAList) {
			this.notReallyJustAList = notReallyJustAList;
		}
	}

	private static final class Bar {

		@Valid
		private final List<@NotNull String> iterable;

		private Bar(List<String> iterable) {
			this.iterable = iterable;
		}
	}

	private static final class FooBar {

		@Valid
		private final FooBarContainer<@NotNull String> fooBarContainer;

		private FooBar(FooBarContainer<String> fooBarContainer) {
			this.fooBarContainer = fooBarContainer;
		}
	}

	private static class FooBarContainer<T> implements Iterable<T>, CustomContainer<T> {

		private final List<T> collection = new ArrayList<>();

		@Override
		public Iterator<T> iterator() {
			return collection.iterator();
		}

		public FooBarContainer<T> add(T element) {
			collection.add( element );
			return this;
		}
	}

	private static class Container extends ArrayList<String> implements CustomContainer<String> {

	}

	private static class BarContainer implements List<String>, CustomContainer<String> {

		private final List<String> container = new ArrayList<>();

		@Override
		public int size() {
			return container.size();
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean contains(Object o) {
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			return container.iterator();
		}

		@Override
		public Object[] toArray() {
			return new Object[0];
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return null;
		}

		@Override
		public boolean add(String s) {
			return container.add( s );
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends String> c) {
			return false;
		}

		@Override
		public boolean addAll(int index, Collection<? extends String> c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Override
		public void clear() {

		}

		@Override
		public String get(int index) {
			return container.get( index );
		}

		@Override
		public String set(int index, String element) {
			return null;
		}

		@Override
		public void add(int index, String element) {

		}

		@Override
		public String remove(int index) {
			return null;
		}

		@Override
		public int indexOf(Object o) {
			return 0;
		}

		@Override
		public int lastIndexOf(Object o) {
			return 0;
		}

		@Override
		public ListIterator<String> listIterator() {
			return null;
		}

		@Override
		public ListIterator<String> listIterator(int index) {
			return null;
		}

		@Override
		public List<String> subList(int fromIndex, int toIndex) {
			return null;
		}
	}

}
