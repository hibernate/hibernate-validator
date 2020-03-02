/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for retrieval of type annotation level constraints.
 *
 * @author Khalid Alqinyah
 */
public class TypeAnnotationMetaDataRetrievalTest extends AnnotationMetaDataProviderTestBase {

	private AnnotationMetaDataProvider provider;

	@BeforeClass
	public void setup() {
		provider = new AnnotationMetaDataProvider(
				getDummyConstraintCreationContext(),
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ),
				new AnnotationProcessingOptionsImpl()
		);
	}

	@Test
	public void testFieldTypeArgument() throws Exception {
		BeanConfiguration<A> beanConfiguration = provider.getBeanConfiguration( A.class );

		ConstrainedField field = findConstrainedField( beanConfiguration, A.class, "names" );
		assertThat( field.getTypeArgumentConstraints().size() ).isEqualTo( 2 );
		assertThat( getAnnotationsTypes( field.getTypeArgumentConstraints() ) ).contains(
				NotNull.class, NotBlank.class
		);
	}

	@Test
	public void testGetterTypeArgument() throws Exception {
		BeanConfiguration<B> beanConfiguration = provider.getBeanConfiguration( B.class );

		ConstrainedExecutable executable = findConstrainedMethod( beanConfiguration, B.class, "getNames" );
		assertThat( executable.getTypeArgumentConstraints().size() ).isEqualTo( 2 );
		assertThat( getAnnotationsTypes( executable.getTypeArgumentConstraints() ) ).contains(
				NotNull.class, NotBlank.class
		);
	}

	@Test
	public void testReturnValueTypeArgument() throws Exception {
		BeanConfiguration<C> beanConfiguration = provider.getBeanConfiguration( C.class );

		ConstrainedExecutable executable = findConstrainedMethod( beanConfiguration, C.class, "returnNames" );
		assertThat( executable.getTypeArgumentConstraints().size() ).isEqualTo( 2 );
		assertThat( getAnnotationsTypes( executable.getTypeArgumentConstraints() ) ).contains(
				NotNull.class, NotBlank.class
		);
	}

	@Test
	public void testExecutableParameterTypeArgument() throws Exception {
		BeanConfiguration<D> beanConfiguration = provider.getBeanConfiguration( D.class );

		ConstrainedExecutable executable = findConstrainedMethod(
				beanConfiguration,
				D.class,
				"setValues",
				String.class,
				Integer.class,
				List.class
		);
		ConstrainedParameter parameter = executable.getParameterMetaData( 2 );
		assertThat( parameter.getTypeArgumentConstraints().size() ).isEqualTo( 2 );
		assertThat( getAnnotationsTypes( parameter.getTypeArgumentConstraints() ) ).contains(
				NotNull.class, NotBlank.class
		);
	}

	@Test
	public void testConstructorParameterTypeArgument() throws Exception {
		BeanConfiguration<E> beanConfiguration = provider.getBeanConfiguration( E.class );

		ConstrainedExecutable executable = findConstrainedConstructor(
				beanConfiguration,
				E.class,
				String.class,
				Integer.class,
				List.class
		);

		ConstrainedParameter parameter = executable.getParameterMetaData( 2 );
		assertThat( parameter.getTypeArgumentConstraints().size() ).isEqualTo( 2 );
		assertThat( getAnnotationsTypes( parameter.getTypeArgumentConstraints() ) ).contains(
				NotNull.class, NotBlank.class
		);
	}

	private List<Class<? extends Annotation>> getAnnotationsTypes(Collection<MetaConstraint<?>> metaConstraints) {
		return metaConstraints.stream()
			.map( m -> m.getDescriptor().getAnnotationType() )
			.collect( Collectors.toList() );
	}

	static class A {
		@Valid
		List<@NotNull @NotBlank String> names;
	}

	static class B {
		@Valid
		public List<@NotNull @NotBlank String> getNames() {
			return Collections.emptyList();
		}
	}

	static class C {
		@Valid
		public List<@NotNull @NotBlank String> returnNames() {
			return Collections.emptyList();
		}
	}

	static class D {
		public void setValues(String s, Integer i, @Valid List<@NotNull @NotBlank String> numbers) {

		}
	}

	static class E {
		public E(String s, Integer i, @Valid List<@NotNull @NotBlank String> numbers) {

		}
	}
}
