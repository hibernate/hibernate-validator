/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
				new TypeResolutionHelper(),
				new ExecutableParameterNameProvider( new DefaultParameterNameProvider() ),
				new ValueExtractorManager( Collections.emptySet() ),
				Collections.<MetaDataProvider>emptyList(),
				new MethodValidationConfiguration.Builder().build()
		);
	}

	@Test
	public void locallyDefinedGroupConversion() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( User1.class ).getMetaDataFor( "addresses" );

		assertThat( property.getCascadables().iterator().next().getCascadingMetaData().convertGroup( Default.class ) ).isEqualTo( BasicPostal.class );
	}

	@Test
	public void groupConversionDefinedInHierarchy() {
		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( User2.class ).getMetaDataFor( "addresses" );

		assertThat( property.getCascadables().iterator().next().getCascadingMetaData().convertGroup( Default.class ) ).isEqualTo( BasicPostal.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void groupConversionInHierarchyWithSameFrom() {
		beanMetaDataManager.getBeanMetaData( User3.class ).getMetaDataFor( "addresses" );
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

}
