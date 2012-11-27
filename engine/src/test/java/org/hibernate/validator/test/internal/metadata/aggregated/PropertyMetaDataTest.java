/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata.aggregated;

import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConvertGroup;
import javax.validation.Valid;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Gunnar Morling
 */
public class PropertyMetaDataTest {

	private BeanMetaDataManager beanMetaDataManager;

	@BeforeMethod
	public void setupBeanMetaDataManager() {
		beanMetaDataManager = new BeanMetaDataManager( new ConstraintHelper() );
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

	@Test
	public void groupConversionInHierarchyAreMerged() {

		PropertyMetaData property = beanMetaDataManager.getBeanMetaData( User3.class ).getMetaDataFor( "addresses" );

		assertThat( property.convertGroup( Default.class ) ).isEqualTo( BasicPostal.class );
		assertThat( property.convertGroup( Complete.class ) ).isEqualTo( FullPostal.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void groupConversionInHierarchyWithSameFrom() {

		beanMetaDataManager.getBeanMetaData( User4.class ).getMetaDataFor( "addresses" );
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
		@ConvertGroup(from = Complete.class, to = FullPostal.class)
		public Set<Address> getAddresses() {
			return super.getAddresses();
		}
	}

	private static class User4 extends User1 {

		@Override
		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		public Set<Address> getAddresses() {
			return super.getAddresses();
		}
	}
}
