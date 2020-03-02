/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.validation.ParameterNameProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManagerImpl;
import org.hibernate.validator.internal.metadata.DefaultBeanMetaDataClassNormalizer;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests creation of {@link org.hibernate.validator.internal.metadata.raw.ConstrainedParameter} in
 * {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class ParameterMetaDataTest {

	private BeanMetaData<CustomerRepository> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {
		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManagerImpl(
				getDummyConstraintCreationContext(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new ExecutableParameterNameProvider( new DefaultParameterNameProvider() ),
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ),
				new DefaultBeanMetaDataClassNormalizer(),
				new ValidationOrderGenerator(),
				Collections.<MetaDataProvider>emptyList(),
				new MethodValidationConfiguration.Builder().build()
		);

		beanMetaData = beanMetaDataManager.getBeanMetaData( CustomerRepository.class );
	}

	@Test
	public void constrainedParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 1 );

		assertFalse( parameterMetaData.isCascading() );
		assertTrue( parameterMetaData.isConstrained() );
		assertEquals( parameterMetaData.getIndex(), 1 );
		assertEquals( parameterMetaData.getName(), "lastName" );
		assertThat( parameterMetaData ).hasSize( 1 );
		assertEquals(
				parameterMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);
	}

	@Test
	public void cascadingParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "saveCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 0 );

		assertTrue( parameterMetaData.isCascading() );
		assertTrue( parameterMetaData.isConstrained() );
		assertEquals( parameterMetaData.getIndex(), 0 );
		assertEquals( parameterMetaData.getName(), "customer" );
		assertThat( parameterMetaData ).isEmpty();
	}

	@Test
	public void unconstrainedParameterMetaData() throws Exception {
		Method method = CustomerRepository.class.getMethod( "updateCustomer", Customer.class );
		assertFalse( beanMetaData.getMetaDataFor( method ).isPresent() );
	}

	@Test
	public void locallyDefinedGroupConversion() throws Exception {
		Method method = CustomerRepository.class.getMethod( "methodWithParameterGroupConversion", Set.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat(
				methodMetaData.getParameterMetaData( 0 )
						.getCascadingMetaData().convertGroup( Default.class )
		).isEqualTo( ValidationGroup.class );
	}

	@Test @TestForIssue( jiraKey = "HV-887" )
	public void parameterNameInInheritanceHierarchy() throws Exception {

		// The bug is due to a random choice for the parameter name used.
		// The first matching method in the class hierarchy will fit (Service or ServiceImpl in our case).
		//
		// The failure rate on my current VM before fixing the bug is 50%.
		// Running it in a loop does not improve the odds of failure: all tests will pass or fail for all loop run.
		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManagerImpl(
				getDummyConstraintCreationContext(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new ExecutableParameterNameProvider( new SkewedParameterNameProvider() ),
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ),
				new DefaultBeanMetaDataClassNormalizer(),
				new ValidationOrderGenerator(),
				Collections.<MetaDataProvider>emptyList(),
				new MethodValidationConfiguration.Builder().build()
		);
		BeanMetaData<ServiceImpl> localBeanMetaData = beanMetaDataManager.getBeanMetaData( ServiceImpl.class );

		Method method = Service.class.getMethod( "sayHello", String.class );
		ExecutableMetaData methodMetaData = localBeanMetaData.getMetaDataFor( method ).get();

		ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( 0 );

		assertEquals( parameterMetaData.getIndex(), 0 );
		assertEquals( parameterMetaData.getName(), "good", "Parameter name from Service should be used, nor ServiceImpl" );
		assertThat( parameterMetaData ).hasSize( 1 );
		assertEquals(
				parameterMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);
	}

	private interface Service {
		void sayHello(@NotNull String world);
	}

	private static class ServiceImpl implements Service {
		@Override
		@ValidateOnExecution(type = ExecutableType.NONE)
		public void sayHello(String world) {
		}
	}

	public class SkewedParameterNameProvider implements ParameterNameProvider {
		private final ParameterNameProvider defaultProvider = new DefaultParameterNameProvider();

		@Override
		public List<String> getParameterNames(Constructor<?> constructor) {
			return defaultProvider.getParameterNames( constructor );
		}

		@Override
		public List<String> getParameterNames(Method method) {
			if ( method.getDeclaringClass().equals( Service.class ) ) {
				// the parameter name we expect
				return Collections.singletonList( "good" );
			}
			else if ( method.getDeclaringClass().equals( ServiceImpl.class ) ) {
				// the parameter name we do not expect
				return Collections.singletonList( "bad" );
			}
			else {
				return defaultProvider.getParameterNames( method );
			}
		}
	}
}
