/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata.provider;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import javax.validation.constraints.Size;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.provider.TypeUseAnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.test.internal.util.NotBlankTypeUse;
import org.hibernate.validator.test.internal.util.NotNullTypeUse;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Tests for {@link org.hibernate.validator.internal.metadata.provider.TypeUseAnnotationMetaDataProvider}.
 *
 * @author Khalid Alqinyah
 */
public class TypeUseAnnotationMetaDataProviderTest extends AnnotationMetaDataProviderTestBase {

	private TypeUseAnnotationMetaDataProvider provider;

	@BeforeClass
	public void setup() {
		provider = new TypeUseAnnotationMetaDataProvider(
				new ConstraintHelper(),
				new DefaultParameterNameProvider(),
				new AnnotationProcessingOptionsImpl()
		);
	}

	@Test
	public void testFormalParameterField() throws Exception {
		// T t;
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedField field1 = findConstrainedField( beanConfigurations, Bar.class, "t" );
		ConstrainedField field2 = findConstrainedField( beanConfigurations, Bar.class, "v" );

		assertThat( field1.getConstraints() ).hasSize( 1 );
		assertThat( field2.getConstraints() ).hasSize( 2 );

		MetaConstraint<?> constraint = field1.getConstraints().iterator().next();
		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotNullTypeUse.class
		);
		assertThat( getAnnotationsTypes( field2 ) ).contains(
				NotBlankTypeUse.class, Size.class
		);
	}

	@Test
	public void testFormalParameterTypeArgument() throws Exception {
		// List<V> vs;
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedField field = findConstrainedField( beanConfigurations, Bar.class, "vs" );
		assertThat( field.getConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( field ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterReturnValue() throws Exception {
		// V returnV() {..}
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedExecutable executable = findConstrainedMethod( beanConfigurations, Bar.class, "returnV" );
		assertThat( executable.getConstraints() ).hasSize( 1 );
		MetaConstraint<?> constraint = executable.getConstraints().iterator().next();
		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterTypeArgumentReturnValue() throws Exception {
		// List<V> returnVs() {..}
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedExecutable executable = findConstrainedMethod( beanConfigurations, Bar.class, "returnVs" );
		assertThat( executable.getConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( executable ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterForExecutableParameters() throws Exception {
		// void setValues(T t, V v, List<V> vs) {..}
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedExecutable executable = findConstrainedMethod( beanConfigurations, Bar.class, "setValues", Object.class, CharSequence.class, List.class );
		ConstrainedParameter firstParameter = executable.getParameterMetaData( 0 );
		ConstrainedParameter secondParameter = executable.getParameterMetaData( 1 );
		ConstrainedParameter thirdParameter = executable.getParameterMetaData( 2 );

		assertThat( firstParameter.getConstraints() ).hasSize( 1 );
		assertThat( secondParameter.getConstraints() ).hasSize( 1 );
		assertThat( thirdParameter.getConstraints() ).hasSize( 2 );

		MetaConstraint<?> constraint = firstParameter.getConstraints().iterator().next();
		MetaConstraint<?> constraint2 = secondParameter.getConstraints().iterator().next();

		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotNullTypeUse.class
		);

		assertThat( constraint2.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotBlankTypeUse.class
		);

		assertThat( getAnnotationsTypes( thirdParameter ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterForConstructorParameters() throws Exception {
		// Bar(T t, V v, List<V> vs) {..}
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		ConstrainedExecutable executable = findConstrainedConstructor( beanConfigurations, Bar.class, Object.class, CharSequence.class, List.class );
		ConstrainedParameter firstParameter = executable.getParameterMetaData( 0 );
		ConstrainedParameter secondParameter = executable.getParameterMetaData( 1 );
		ConstrainedParameter thirdParameter = executable.getParameterMetaData( 2 );

		assertThat( firstParameter.getConstraints() ).hasSize( 1 );
		assertThat( secondParameter.getConstraints() ).hasSize( 1 );
		assertThat( thirdParameter.getConstraints() ).hasSize( 2 );

		MetaConstraint<?> constraint = firstParameter.getConstraints().iterator().next();
		MetaConstraint<?> constraint2 = secondParameter.getConstraints().iterator().next();

		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotNullTypeUse.class
		);

		assertThat( constraint2.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotBlankTypeUse.class
		);

		assertThat( getAnnotationsTypes( thirdParameter ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterSuperclassFields() throws Exception {
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		// For BarBase fields
		ConstrainedField field1 = findConstrainedField( beanConfigurations, BarBase.class, "s" );
		ConstrainedField field2 = findConstrainedField( beanConfigurations, BarBase.class, "u" );

		assertThat( field1.getConstraints() ).hasSize( 1 );
		assertThat( field2.getConstraints() ).hasSize( 1 );

		MetaConstraint<?> constraint1 = field1.getConstraints().iterator().next();
		MetaConstraint<?> constraint2 = field2.getConstraints().iterator().next();

		assertThat( constraint1.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotNullTypeUse.class
		);

		assertThat( constraint2.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotBlankTypeUse.class
		);
	}

	@Test
	public void testFormalParameterInterfaceReturnValues() throws Exception {
		List<BeanConfiguration<? super Bar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Bar.class
		);

		// For BarInt return values
		ConstrainedExecutable executable1 = findConstrainedMethod( beanConfigurations, BarInt.class, "getM" );
		ConstrainedExecutable executable2 = findConstrainedMethod( beanConfigurations, BarInt.class, "getN" );

		assertThat( executable1.getConstraints() ).hasSize( 1 );
		assertThat( executable2.getConstraints() ).hasSize( 1 );

		MetaConstraint<?> constraint1 = executable1.getConstraints().iterator().next();
		MetaConstraint<?> constraint2 = executable2.getConstraints().iterator().next();

		assertThat( constraint1.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotNullTypeUse.class
		);

		assertThat( constraint2.getDescriptor().getAnnotation().annotationType() ).isEqualTo(
				NotBlankTypeUse.class
		);
	}

	private List<Class<? extends Annotation>> getAnnotationsTypes(ConstrainedElement constrainedElement) {
		List<Class<? extends Annotation>> annotationsTypes = newArrayList();
		Iterator<MetaConstraint<?>> iter = constrainedElement.getConstraints().iterator();
		while ( iter.hasNext() ) {
			annotationsTypes.add( iter.next().getDescriptor().getAnnotation().annotationType() );
		}
		return annotationsTypes;
	}

	static class Bar<@NotNullTypeUse T, @NotBlankTypeUse V extends CharSequence> extends BarBase<@NotNullTypeUse Integer, @NotBlankTypeUse String> implements BarInt<@NotNullTypeUse Integer, @NotBlankTypeUse String> {
		T t;

		@Size
		V v;

		List<@NotNullTypeUse V> vs;

		public Bar(T t, V v, List<@NotNullTypeUse V> vs) {

		}

		public void setValues(T t, V v, List<@NotNullTypeUse V> vs) {

		}

		public V returnV() {
			return v;
		}

		public List<@NotNullTypeUse V> returnVs() {
			return vs;
		}

		public Integer getM() {
			return null;
		}

		public String getN() {
			return "";
		}
	}
}

class BarBase<S, U extends CharSequence> {
	S s;

	U u;
}

interface BarInt<M, N extends CharSequence> {
	M getM();

	N getN();
}
