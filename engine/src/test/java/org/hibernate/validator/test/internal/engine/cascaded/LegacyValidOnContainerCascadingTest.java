/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

public class LegacyValidOnContainerCascadingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnList() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ValidOnList>> constraintViolations = validator.validate( ValidOnList.invalid() );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "listName" ),
				pathWith()
						.property( "visitors" )
						.property( "name", true, null, 0, MyList.class, 0 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnListAndOnTypeArgument() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ValidOnListAndOnTypeArgument>> constraintViolations = validator.validate( ValidOnListAndOnTypeArgument.invalid() );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "listName" ),
				pathWith()
						.property( "visitors" )
						.property( "name", true, null, 0, MyList.class, 0 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnListAndOnTypeArgumentWithGroupConversions() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ValidOnListAndOnTypeArgumentWithGroupConversions>> constraintViolations =
				validator.validate( ValidOnListAndOnTypeArgumentWithGroupConversions.invalid() );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "extended1" ),
				pathWith()
						.property( "visitors" )
						.property( "name", true, null, 0, MyListWithGroupConversions.class, 0 )
		);

		constraintViolations = validator.validate( ValidOnListAndOnTypeArgumentWithGroupConversions.invalid(), ExtendedChecks1.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "extended1" ),
				pathWith()
						.property( "visitors" )
						.property( "extended2", true, null, 0, MyListWithGroupConversions.class, 0 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnIterable() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ValidOnIterable>> constraintViolations = validator.validate( ValidOnIterable.invalid() );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "listName" ),
				pathWith()
						.property( "visitors" )
						.property( "name", true, null, null, MySet.class, 0 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnMap() {
		Validator validator = getValidator();

		Museum museum = Museum.invalid();

		Set<ConstraintViolation<ValidOnMap>> constraintViolations = validator.validate( ValidOnMap.invalid( museum ) );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitors" )
						.property( "listName" ),
				pathWith()
						.property( "visitors" )
						.property( "name", true, museum, null, MyMap.class, 1 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1344")
	public void testValidOnOptional() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ValidOnOptional>> constraintViolations = validator.validate( ValidOnOptional.invalid() );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "visitor" )
						.property( "name", false, null, null, Optional.class, 0 )
		);
	}

	private static class ValidOnList {

		@Valid
		private final MyList<Visitor> visitors;

		private ValidOnList(MyList<Visitor> visitors) {
			this.visitors = visitors;
		}

		private static ValidOnList invalid() {
			return new ValidOnList( new MyList<Visitor>( null, Arrays.asList( new Visitor( null ) ) ) );
		}
	}

	private static class ValidOnListAndOnTypeArgument {

		@Valid
		private final MyList<@Valid Visitor> visitors;

		private ValidOnListAndOnTypeArgument(MyList<Visitor> visitors) {
			this.visitors = visitors;
		}

		private static ValidOnListAndOnTypeArgument invalid() {
			return new ValidOnListAndOnTypeArgument( new MyList<Visitor>( null, Arrays.asList( new Visitor( null ) ) ) );
		}
	}

	private static class MyList<E> extends ArrayList<E> {

		@NotNull
		private final String listName;

		private MyList(String listName, List<E> elements) {
			this.listName = listName;
			addAll( elements );
		}
	}

	private static class Visitor {

		@NotNull
		private final String name;

		private Visitor(String name) {
			this.name = name;
		}
	}

	interface ExtendedChecks1 {
	}

	interface ExtendedChecks2 {
	}

	private static class ValidOnIterable {

		@Valid
		private final MySet<Visitor> visitors;

		private ValidOnIterable(MySet<Visitor> visitors) {
			this.visitors = visitors;
		}

		private static ValidOnIterable invalid() {
			return new ValidOnIterable( new MySet<Visitor>( null, Arrays.asList( new Visitor( null ) ) ) );
		}
	}

	private static class MySet<E> extends HashSet<E> {

		@NotNull
		private final String listName;

		private MySet(String listName, List<E> elements) {
			this.listName = listName;
			addAll( elements );
		}
	}

	private static class ValidOnMap {

		@Valid
		private final MyMap<Museum, Visitor> visitors;

		private ValidOnMap(MyMap<Museum, Visitor> visitors) {
			this.visitors = visitors;
		}

		private static ValidOnMap invalid(Museum museum) {
			Map<Museum, Visitor> map = new HashMap<>();
			map.put( museum, new Visitor( null ) );

			return new ValidOnMap( new MyMap<Museum, Visitor>( null, map ) );
		}
	}

	private static class MyMap<K, V> extends HashMap<K, V> {

		@NotNull
		private final String listName;

		private MyMap(String listName, Map<K, V> elements) {
			this.listName = listName;
			putAll( elements );
		}
	}

	private static class Museum {

		@NotNull
		private final String name;

		public Museum(String name) {
			this.name = name;
		}

		private static Museum invalid() {
			return new Museum( null );
		}
	}

	private static class ValidOnOptional {

		@Valid
		private final Optional<Visitor> visitor;

		private ValidOnOptional(Optional<Visitor> visitor) {
			this.visitor = visitor;
		}

		private static ValidOnOptional invalid() {
			return new ValidOnOptional( Optional.of( new Visitor( null ) ) );
		}
	}
}
