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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.provider.TypeAnnotationAwareMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.testutil.constraints.NotBlankTypeUse;
import org.hibernate.validator.testutil.constraints.NotNullTypeUse;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Tests for {@link org.hibernate.validator.internal.metadata.provider.TypeAnnotationAwareMetaDataProvider}.
 *
 * @author Khalid Alqinyah
 */
public class TypeAnnotationAwareMetaDataProviderTest extends AnnotationMetaDataProviderTestBase {

	private TypeAnnotationAwareMetaDataProvider provider;

	@BeforeClass
	public void setup() {
		provider = new TypeAnnotationAwareMetaDataProvider(
				new ConstraintHelper(),
				new DefaultParameterNameProvider(),
				new AnnotationProcessingOptionsImpl()
		);
	}

	@Test
	public void testFieldTypeArgument() throws Exception {
		List<BeanConfiguration<? super A>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				A.class
		);

		ConstrainedField field = findConstrainedField( beanConfigurations, A.class, "names" );
		assertThat( field.getTypeArgumentsConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( field.getTypeArgumentsConstraints() ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testGetterTypeArgument() throws Exception {
		List<BeanConfiguration<? super B>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				B.class
		);

		ConstrainedExecutable executable = findConstrainedMethod( beanConfigurations, B.class, "getNames" );
		assertThat( executable.getTypeArgumentsConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( executable.getTypeArgumentsConstraints() ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testReturnValueTypeArgument() throws Exception {
		List<BeanConfiguration<? super C>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				C.class
		);

		ConstrainedExecutable executable = findConstrainedMethod( beanConfigurations, C.class, "returnNames" );
		assertThat( executable.getTypeArgumentsConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( executable.getTypeArgumentsConstraints() ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testExecutableParameterTypeArgument() throws Exception {
		List<BeanConfiguration<? super D>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				D.class
		);

		ConstrainedExecutable executable = findConstrainedMethod(
				beanConfigurations,
				D.class,
				"setValues",
				String.class,
				Integer.class,
				List.class
		);
		ConstrainedParameter parameter = executable.getParameterMetaData( 2 );
		assertThat( parameter.getTypeArgumentsConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( parameter.getTypeArgumentsConstraints() ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	@Test
	public void testConstructorParameterTypeArgument() throws Exception {
		List<BeanConfiguration<? super E>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				E.class
		);

		ConstrainedExecutable executable = findConstrainedConstructor(
				beanConfigurations,
				E.class,
				String.class,
				Integer.class,
				List.class
		);

		ConstrainedParameter parameter = executable.getParameterMetaData( 2 );
		assertThat( parameter.getTypeArgumentsConstraints() ).hasSize( 2 );
		assertThat( getAnnotationsTypes( parameter.getTypeArgumentsConstraints() ) ).contains(
				NotNullTypeUse.class, NotBlankTypeUse.class
		);
	}

	private List<Class<? extends Annotation>> getAnnotationsTypes(Set<MetaConstraint<?>> metaConstraints) {
		List<Class<? extends Annotation>> annotationsTypes = newArrayList();
		Iterator<MetaConstraint<?>> iter = metaConstraints.iterator();
		while ( iter.hasNext() ) {
			annotationsTypes.add( iter.next().getDescriptor().getAnnotation().annotationType() );
		}
		return annotationsTypes;
	}

	static class A {
		@Valid
		List<@NotNullTypeUse @NotBlankTypeUse String> names;
	}

	static class B {
		@Valid
		public List<@NotNullTypeUse @NotBlankTypeUse String> getNames() {
			return Collections.emptyList();
		}
	}

	static class C {
		@Valid
		public List<@NotNullTypeUse @NotBlankTypeUse String> returnNames() {
			return Collections.emptyList();
		}
	}

	static class D {
		public void setValues(String s, Integer i, @Valid List<@NotNullTypeUse @NotBlankTypeUse String> numbers) {

		}
	}

	static class E {
		public E(String s, Integer i, @Valid List<@NotNullTypeUse @NotBlankTypeUse String> numbers) {

		}
	}
}
