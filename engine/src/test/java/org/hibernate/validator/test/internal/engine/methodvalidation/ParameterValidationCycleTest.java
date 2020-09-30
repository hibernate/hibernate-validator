/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;

/**
 * @author Guillaume Smet
 * @author Chris Westmorland
 */
public class ParameterValidationCycleTest {

	@Test
	@TestForIssue(jiraKey = "HV-1797")
	public void testParameterValidationCycle() throws NoSuchMethodException, SecurityException {
		final Parent parent = new Parent();
		parent.setId( 1L );

		final Child child = new Child();
		child.setId( null );
		child.setParent( parent );

		parent.getChildren().add( child );

		ExecutableValidator executableValidator = ValidatorUtil.getValidator().forExecutables();
		Set<ConstraintViolation<ExecutableHolder>> violations = executableValidator.validateParameters( new ExecutableHolder(),
				ExecutableHolder.class.getDeclaredMethod( "post", Parent.class ),
				new Object[]{ parent } );
		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations( violationOf( NotNull.class ) );
	}

	private static class ExecutableHolder {

		@SuppressWarnings("unused")
		public void post(@Valid @NotNull Parent parent) {
		}
	}

	@SuppressWarnings("unused")
	private static class Parent {

		@NotNull
		private Long id;

		@Valid
		private Set<Child> children = new HashSet<>();

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Set<Child> getChildren() {
			return children;
		}

		public void setChildren(Set<Child> children) {
			this.children = children;
		}
	}

	@SuppressWarnings("unused")
	private static class Child {

		@NotNull
		private Long id;

		@NotNull
		@Valid
		private Parent parent;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Parent getParent() {
			return parent;
		}

		public void setParent(Parent parent) {
			this.parent = parent;
		}
	}
}
