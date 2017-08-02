/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

import javax.validation.ElementKind;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.test.internal.metadata.ConsistentDateParameters;
import org.hibernate.validator.test.internal.metadata.Customer;
import org.hibernate.validator.test.internal.metadata.CustomerRepository.ValidationGroup;
import org.hibernate.validator.test.internal.metadata.CustomerRepositoryExt;
import org.hibernate.validator.testutil.TestForIssue;
import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests creation of {@link ExecutableMetaData} in {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl}.
 *
 * @author Gunnar Morling
 */
public class ExecutableMetaDataTest {

	private BeanMetaDataManager beanMetaDataManager;
	private BeanMetaData<CustomerRepositoryExt> beanMetaData;

	@BeforeMethod
	public void setupBeanMetaData() {
		beanMetaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new TypeResolutionHelper(),
				new ExecutableParameterNameProvider( new DefaultParameterNameProvider() ),
				new ValueExtractorManager( Collections.emptySet() ),
				new ValidationOrderGenerator(),
				Collections.<MetaDataProvider>emptyList(),
				new MethodValidationConfiguration.Builder().build()
		);

		beanMetaData = beanMetaDataManager.getBeanMetaData( CustomerRepositoryExt.class );
	}

	@Test
	public void getNameOfMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getName() ).isEqualTo( method.getName() );
	}

	@Test
	public void getNameOfConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor( constructor ).get();

		assertThat( constructorMetaData.getName() ).isEqualTo( constructor.getDeclaringClass().getSimpleName() );
	}

	@Test
	public void getTypeForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getType() ).isEqualTo( Customer.class );
	}

	@Test
	public void getTypeForVoidMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "zap", Integer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getType() ).isEqualTo( void.class );
	}

	@Test
	public void getTypeForConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor( constructor ).get();

		assertThat( constructorMetaData.getType() ).isEqualTo( CustomerRepositoryExt.class );
	}

	@Test
	public void getParameterTypes() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getParameterTypes() ).isEqualTo( method.getParameterTypes() );
	}

	@Test
	public void getParameterTypesForParameterlessExcutable() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getParameterTypes() ).isEqualTo( method.getParameterTypes() );
	}

	@Test
	public void getKindForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getKind() ).isEqualTo( ElementKind.METHOD );
	}

	@Test
	public void getKindForConstructor() throws Exception {

		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor( constructor ).get();

		assertThat( constructorMetaData.getKind() ).isEqualTo( ElementKind.CONSTRUCTOR );
	}

	@Test
	public void getIdentifierForMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() ).containsOnly(
				"createCustomer(java.lang.CharSequence,java.lang.String)"
		);
	}

	@Test
	public void getIdentifierForParameterlessMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() ).containsOnly( "foo()" );
	}

	@Test
	public void getIdentifierForConstructor() throws Exception {
		Constructor<CustomerRepositoryExt> constructor = CustomerRepositoryExt.class.getConstructor( String.class );
		ExecutableMetaData constructorMetaData = beanMetaData.getMetaDataFor( constructor ).get();

		assertThat( constructorMetaData.getSignatures() ).containsOnly( "CustomerRepositoryExt(java.lang.String)" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1011")
	public void getIdentifierForOverridingGenericMethod() throws Exception {
		Method method = JobRepositoryImpl.class.getMethod( "createJob", UUID.class );
		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( JobRepositoryImpl.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() )
			.describedAs( "Expecting super-type and sub-type method signatures" )
			.containsOnly( "createJob(java.lang.Object)", "createJob(java.util.UUID)" );

		method = JobRepository.class.getMethod( "createJob", Object.class );
		beanMetaData = beanMetaDataManager.getBeanMetaData( JobRepository.class );
		methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() )
			.describedAs( "Expecting only super-type method signature" )
			.containsOnly( "createJob(java.lang.Object)" );

		method = SpecialJobRepositoryImpl.class.getMethod( "createJob", Object.class );
		beanMetaData = beanMetaDataManager.getBeanMetaData( SpecialJobRepositoryImpl.class );
		methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() )
			.describedAs( "Expecting method signatures from super-types" )
			.containsOnly( "createJob(java.lang.Object)", "createJob(java.util.UUID)" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1011")
	public void getIdentifierForOverloadedMethod() throws Exception {
		Method method = SpecialJobRepositoryImpl.class.getMethod( "createJob", String.class );
		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( SpecialJobRepositoryImpl.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData.getSignatures() )
			.describedAs( "Expecting sub-type method signature which overloads but not overrides super-type methods" )
			.containsOnly( "createJob(java.lang.String)" );
	}

	@Test
	public void methodWithConstrainedParameter() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "createCustomer", CharSequence.class, String.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertFalse( methodMetaData.getParameterMetaData( 0 ).isConstrained() );
		assertFalse( methodMetaData.getParameterMetaData( 0 ).isCascading() );

		assertTrue( methodMetaData.getParameterMetaData( 1 ).isConstrained() );
		assertFalse( methodMetaData.getParameterMetaData( 1 ).isCascading() );
		assertThat( methodMetaData.getParameterMetaData( 1 ) ).hasSize( 1 );
		assertEquals(
				methodMetaData.getParameterMetaData( 1 )
						.iterator()
						.next()
						.getDescriptor()
						.getAnnotation()
						.annotationType(),
				NotNull.class
		);

		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void methodWithCascadedParameter() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "saveCustomer", Customer.class );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertTrue( methodMetaData.getParameterMetaData( 0 ).isConstrained() );
		assertTrue( methodMetaData.getParameterMetaData( 0 ).isCascading() );
		assertThat( methodMetaData.getParameterMetaData( 0 ) ).isEmpty();

		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void methodWithCrossParameterConstraint() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod(
				"methodWithCrossParameterConstraint",
				DateMidnight.class,
				DateMidnight.class
		);
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();

		assertThat( methodMetaData.getCrossParameterConstraints() ).hasSize( 1 );
		assertThat(
				methodMetaData.getCrossParameterConstraints()
						.iterator()
						.next()
						.getDescriptor()
						.getAnnotation()
						.annotationType()
		).isEqualTo( ConsistentDateParameters.class );
	}

	@Test
	public void methodWithConstrainedReturnValue() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "bar" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 1 );
		assertEquals(
				methodMetaData.iterator().next().getDescriptor().getAnnotation().annotationType(), NotNull.class
		);

		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void returnValueConstraintFromSuperType() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "bar" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat( methodMetaData ).hasSize( 1 );
		assertFalse( methodMetaData.isCascading() );

		ConstraintDescriptorImpl<? extends Annotation> descriptor = methodMetaData.iterator()
				.next()
				.getDescriptor();
		assertEquals( descriptor.getAnnotation().annotationType(), NotNull.class );
	}

	@Test
	public void returnValueConstraintsAddUpInHierarchy() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "baz" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertFalse( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).hasSize( 2 );
	}

	@Test
	public void methodWithCascadedReturnValue() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "foo" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertTrue( methodMetaData.isCascading() );
		assertTrue( methodMetaData.isConstrained() );
		assertThat( methodMetaData ).isEmpty();
		assertThat( methodMetaData.getCrossParameterConstraints() ).isEmpty();
	}

	@Test
	public void locallyDefinedGroupConversion() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "methodWithReturnValueGroupConversion" );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( method ).get();

		assertThat(
				methodMetaData.getReturnValueMetaData()
						.getCascadables()
						.iterator()
						.next()
						.getCascadingMetaData()
						.convertGroup( Default.class )
		).isEqualTo( ValidationGroup.class );
	}

	@Test
	public void unconstrainedMethod() throws Exception {
		Method method = CustomerRepositoryExt.class.getMethod( "updateCustomer", Customer.class );

		assertFalse( beanMetaData.getMetaDataFor( method ).isPresent() );
	}
}
