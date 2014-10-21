/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

/**
 * @author Gunnar Morling
 */
public class PropertyMetaDataTest {

	private BeanMetaDataManager beanMetaDataManager;

	@BeforeMethod
	public void setupBeanMetaDataManager() {
		beanMetaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new DefaultParameterNameProvider(),
				Collections.<MetaDataProvider>emptyList()
		);
	}

	@Test
	public void locallyDefinedGroupConversion() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( User1.class ).getMetaDataFor( "addresses" );

		assertThat( property.convertGroup( Default.class ) ).isEqualTo( BasicPostal.class );
	}

	@Test
	public void groupConversionDefinedInHierarchy() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( User2.class ).getMetaDataFor( "addresses" );

		assertThat( property.convertGroup( Default.class ) ).isEqualTo( BasicPostal.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void groupConversionInHierarchyWithSameFrom() {
		beanMetaDataManager.getBeanMetaData( User3.class ).getMetaDataFor( "addresses" );
	}

	@Test
	public void unwrapValidatedValueGivenOnField() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( Customer.class ).getMetaDataFor( "name" );
		assertEquals( property.unwrapMode(), UnwrapMode.UNWRAP );

		property = beanMetaDataManager.getBeanMetaData( Customer.class ).getMetaDataFor( "age" );
		assertEquals( property.unwrapMode(), UnwrapMode.AUTOMATIC );
	}

	@Test
	public void unwrapValidatedValueGivenOnProperty() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( Customer.class ).getMetaDataFor( "firstName" );
		assertEquals( property.unwrapMode(), UnwrapMode.UNWRAP );
	}

	@Test
	public void unwrapValidatedValueGivenOnPropertyInSuperClass() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( RetailCustomer.class )
				.getMetaDataFor( "firstName" );
		assertEquals( property.unwrapMode(), UnwrapMode.UNWRAP );
	}

	@TestForIssue( jiraKey = "HV-925")
	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000189.*")
	public void inconsistent_unwrap_configuration_between_field_and_getter_throws_exception() {
		beanMetaDataManager.getBeanMetaData( DiscountCustomer.class ).getMetaDataFor( "firstName" );
	}

	public interface Complete extends Default {
	}

	public interface BasicPostal {
	}

	public interface FullPostal extends BasicPostal {
	}

	private static class Address {
	}

	private static class User1 {

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		public Set<Address> getAddresses() {
			return null;
		}
	}

	private static class User2 extends User1 {

		@Override
		public Set<Address> getAddresses() {
			return super.getAddresses();
		}
	}

	private static class User3 extends User1 {

		@Override
		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Default.class, to = Complete.class)
		})
		public Set<Address> getAddresses() {
			return super.getAddresses();
		}
	}

	@SuppressWarnings("unused")
	private static class Customer {

		@UnwrapValidatedValue
		private String name;

		private int age;

		private String firstName;

		@UnwrapValidatedValue
		public String getFirstName() {
			return firstName;
		}
	}

	private static class RetailCustomer extends Customer {
	}

	@SuppressWarnings("unused")
	private static class DiscountCustomer {

		@UnwrapValidatedValue(false)
		private String firstName;

		@UnwrapValidatedValue
		public String getFirstName() {
			return firstName;
		}
	}
}
