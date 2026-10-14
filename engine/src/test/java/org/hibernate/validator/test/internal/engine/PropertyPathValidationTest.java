/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1316")
public class PropertyPathValidationTest {
	private final Validator validator = getValidator();

	@DataProvider
	public Object[][] nonCascadingRoots() {
		return new Object[][] { { new PlainRoot() }, { new ConstrainedRoot() } };
	}

	@Test(dataProvider = "nonCascadingRoots")
	public void intermediateAssociationRequiresValid(Object root) {
		for ( String path : new String[] { "leaf.required", "leaf.optional", "leaf.missing" } ) {
			expectThrows( IllegalArgumentException.class, () -> validator.validateProperty( root, path ) );
		}
	}

	@Test(dataProvider = "nonCascadingRoots")
	public void directExistingPropertyWithoutApplicableConstraints(Object root) {
		assertTrue( validator.validateProperty( root, "leaf" ).isEmpty() );
		assertTrue( validator.validateValue( root.getClass(), "leaf", new Leaf() ).isEmpty() );
	}

	@Test(dataProvider = "nonCascadingRoots")
	public void missingRootPropertyIsRejected(Object root) {
		expectThrows( IllegalArgumentException.class, () -> validator.validateProperty( root, "missing" ) );
		expectThrows( IllegalArgumentException.class, () -> validator.validateValue( root.getClass(), "missing", null ) );
	}

	@Test
	public void nestedLeafConstraintsAndViolationContext() {
		CascadingRoot root = new CascadingRoot();
		var violations = validator.validateProperty( root, "leaf.required" );
		assertEquals( violations.size(), 1 );
		var violation = violations.iterator().next();
		assertSame( violation.getRootBean(), root );
		assertSame( violation.getLeafBean(), root.leaf );
		assertEquals( violation.getPropertyPath().toString(), "leaf.required" );
		assertTrue( validator.validateProperty( root, "leaf.optional" ).isEmpty() );
		assertTrue( validator.validateProperty( root, "leaf" ).isEmpty() );
		expectThrows( IllegalArgumentException.class, () -> validator.validateProperty( root, "leaf.missing" ) );
	}

	@Test
	public void everyIntermediateAssociationRequiresValid() {
		DeepRoot root = new DeepRoot();
		assertEquals( validator.validateProperty( root, "branch.leaf.required" ).size(), 1 );
		assertTrue( validator.validateProperty( root, "branch.leaf.optional" ).isEmpty() );
		expectThrows( IllegalArgumentException.class, () -> validator.validateProperty( root, "branch.unmarked.required" ) );
	}

	@Test
	public void leafCanBeValidatedDirectly() {
		Leaf leaf = new Leaf();
		var violation = validator.validateProperty( leaf, "required" ).iterator().next();
		assertSame( violation.getRootBean(), leaf );
		assertEquals( violation.getPropertyPath().toString(), "required" );
		assertTrue( validator.validateProperty( leaf, "optional" ).isEmpty() );
	}

	@Test
	public void entirelyUnconstrainedLeafReturnsEmptyForExistingProperty() {
		assertTrue( validator.validateProperty( new PlainLeaf(), "optional" ).isEmpty() );
		assertTrue( validator.validateValue( PlainLeaf.class, "optional", null ).isEmpty() );
		assertTrue( validator.validateProperty( new PlainLeafRoot(), "leaf.optional" ).isEmpty() );
		expectThrows( IllegalArgumentException.class, () -> validator.validateProperty( new PlainLeafRoot(), "leaf.missing" ) );
	}

	public static class Leaf {
		@NotNull
		public String required;
		@NotNull
		public String otherRequired;
		public String optional;
	}

	public static class PlainRoot {
		public Leaf leaf = new Leaf();
	}

	public static class ConstrainedRoot {
		@NotNull
		public String unrelated = "present";
		public Leaf leaf = new Leaf();
	}

	public static class CascadingRoot {
		@Valid
		public Leaf leaf = new Leaf();
	}

	public static class Branch extends CascadingRoot {
		public Leaf unmarked = new Leaf();
	}

	public static class DeepRoot {
		@Valid
		public Branch branch = new Branch();
	}

	public static class PlainLeaf {
		public String optional;
	}

	public static class PlainLeafRoot {
		@Valid
		public PlainLeaf leaf = new PlainLeaf();
	}
}
