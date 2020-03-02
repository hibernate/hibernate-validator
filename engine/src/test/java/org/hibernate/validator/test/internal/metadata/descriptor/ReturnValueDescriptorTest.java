/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintTypes;
import static org.hibernate.validator.testutil.DescriptorAssert.assertThat;
import static org.hibernate.validator.testutils.ValidatorUtil.getMethodReturnValueDescriptor;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.GroupConversionDescriptor;
import jakarta.validation.metadata.ReturnValueDescriptor;
import jakarta.validation.metadata.Scope;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerRepositoryExtBasic;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt.CustomerRepositoryExtReturnValueComplex;
import org.hibernate.validator.testutil.TestForIssue;

/**
 * @author Hardy Ferentschik
 */
public class ReturnValueDescriptorTest {

	@Test
	public void testIsCascaded() {
		ReturnValueDescriptor elementDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"foo"
		);
		assertTrue( elementDescriptor.isCascaded() );
	}

	@Test
	public void testIsNotCascaded() {
		ReturnValueDescriptor elementDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"bar"
		);
		assertFalse( elementDescriptor.isCascaded() );
	}

	@Test
	public void testHasConstraints() {
		ReturnValueDescriptor unconstrainedReturnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"foo"
		);
		assertFalse( unconstrainedReturnValueDescriptor.hasConstraints() );

		ReturnValueDescriptor constrainedReturnDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"bar"
		);
		assertTrue( constrainedReturnDescriptor.hasConstraints() );
	}

	@Test
	public void testHasConstraintsConsidersConstraintsFromSuperType() {
		ReturnValueDescriptor constrainedReturnDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class,
				"bar"
		);
		assertTrue( constrainedReturnDescriptor.hasConstraints() );
	}

	@Test
	public void testGetConstraintDescriptors() {
		ReturnValueDescriptor unconstrainedReturnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"foo"
		);
		assertTrue( unconstrainedReturnValueDescriptor.getConstraintDescriptors().isEmpty() );

		ReturnValueDescriptor constrainedReturnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepository.class,
				"bar"
		);
		assertConstraintTypes(
				constrainedReturnValueDescriptor.getConstraintDescriptors(),
				NotNull.class
		);
	}

	@Test
	public void testGetConstraintDescriptorsConsidersConstraintsFromSuperType() {
		ReturnValueDescriptor returnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);
		assertConstraintTypes(
				returnValueDescriptor.getConstraintDescriptors(),
				Min.class,
				NotNull.class
		);
	}

	@TestForIssue(jiraKey = "HV-443")
	@Test
	public void testConstraintsLookingAt() {
		ReturnValueDescriptor returnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);

		assertConstraintTypes(
				returnValueDescriptor.findConstraints()
						.lookingAt( Scope.LOCAL_ELEMENT )
						.getConstraintDescriptors(), Min.class
		);
		assertConstraintTypes(
				returnValueDescriptor.findConstraints()
						.lookingAt( Scope.HIERARCHY )
						.getConstraintDescriptors(), Min.class, NotNull.class
		);
	}

	@Test
	public void testFindConstraintMatchingGroups() {
		ReturnValueDescriptor returnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class,
				"baz"
		);
		assertConstraintTypes(
				returnValueDescriptor.findConstraints()
						.unorderedAndMatchingGroups( ValidationGroup.class )
						.getConstraintDescriptors(), NotNull.class
		);
	}

	@Test
	public void testGetGroupConversions() {
		ReturnValueDescriptor returnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class, "modifyCustomer", Customer.class
		);

		Set<GroupConversionDescriptor> groupConversions = returnValueDescriptor.getGroupConversions();

		assertThat( groupConversions ).hasSize( 2 );
		assertThat( groupConversions ).containsConversion(
				CustomerRepositoryExtBasic.class,
				CustomerBasic.class
		);
		assertThat( groupConversions ).containsConversion(
				CustomerRepositoryExtReturnValueComplex.class,
				CustomerComplex.class
		);
	}

	@Test
	public void testDescriptorForVoidMethod() {
		ReturnValueDescriptor returnValueDescriptor = getMethodReturnValueDescriptor(
				CustomerRepositoryExt.class, "saveCustomer", Customer.class
		);

		assertThat( returnValueDescriptor.getElementClass() ).isSameAs( void.class );
		assertThat( returnValueDescriptor.hasConstraints() ).isFalse();
		assertThat( returnValueDescriptor.isCascaded() ).isFalse();
		assertThat( returnValueDescriptor.getConstraintDescriptors() ).isEmpty();
		assertThat( returnValueDescriptor.getGroupConversions() ).isEmpty();
		assertThat( returnValueDescriptor.findConstraints().getConstraintDescriptors() ).isEmpty();
	}
}
