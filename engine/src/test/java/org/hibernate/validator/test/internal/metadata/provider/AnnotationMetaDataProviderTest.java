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
package org.hibernate.validator.test.internal.metadata.provider;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedMethod;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Unit test for {@link AnnotationMetaDataProvider}.
 *
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProviderTest {

	private AnnotationMetaDataProvider provider;

	@BeforeMethod
	public void setUpProvider() {
		provider = new AnnotationMetaDataProvider(
				new ConstraintHelper(),
				new DefaultParameterNameProvider(),
				new AnnotationProcessingOptions()
		);
	}

	@Test
	public void testGetConstructorMetaData() throws Exception {

		List<BeanConfiguration<? super Foo>> beanConfigurations = provider.getBeanConfigurationForHierarchy( Foo.class );

		assertThat( beanConfigurations ).hasSize( 2 );

		Set<ConstrainedElement> constrainedElements = beanConfigurations.get( 0 ).getConstrainedElements();

		assertThat( constrainedElements ).hasSize( 1 );
		assertThat(
				constrainedElements.iterator()
						.next()
						.getLocation()
						.getMember()
		).isEqualTo( Foo.class.getConstructor( String.class ) );
	}

	@Test
	public void testGetCrossParameterMetaData() throws Exception {

		//when
		List<BeanConfiguration<? super Calendar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Calendar.class
		);

		ConstrainedMethod createEvent = findConstrainedExecutable( beanConfigurations, Calendar.class, "createEvent" );

		//then
		assertThat( createEvent.isConstrained() ).isTrue();
		assertThat( createEvent.isCascading() ).isFalse();
		assertThat( createEvent.getKind() ).isEqualTo( ConstrainedElementKind.METHOD );
		assertThat( createEvent.getConstraints() ).as( "No return value constraints expected" ).isEmpty();
		assertThat( createEvent.getCrossParameterConstraints() ).hasSize( 1 );

		ConstraintLocation location = createEvent.getLocation();

		assertThat( location.getMember() ).isEqualTo(
				Calendar.class.getMethod(
						"createEvent",
						DateMidnight.class,
						DateMidnight.class
				)
		);
		assertThat( location.getElementType() ).isEqualTo( ElementType.METHOD );

		MetaConstraint<?> constraint = createEvent.getCrossParameterConstraints().iterator().next();

		assertThat(
				constraint.getDescriptor()
						.getAnnotation()
						.annotationType()
		).isEqualTo( ConsistentDateParameters.class );
		assertThat( constraint.getLocation().typeOfAnnotatedElement() ).isEqualTo( Object[].class );
	}

	private <T> ConstrainedMethod findConstrainedExecutable(Iterable<BeanConfiguration<? super T>> beanConfigurations, Class<T> beanType, String executableName) {

		BeanConfiguration<? super T> beanConfiguration = null;

		for ( BeanConfiguration<? super T> oneConfiguration : beanConfigurations ) {
			if ( oneConfiguration.getBeanClass().equals( beanType ) ) {
				beanConfiguration = oneConfiguration;
				break;
			}
		}

		if ( beanConfiguration == null ) {
			throw new RuntimeException( "Found no configuration for type " + beanType );
		}


		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getLocation().getMember().getName().equals( executableName ) ) {
				return (ConstrainedMethod) constrainedElement;
			}
		}

		throw new RuntimeException( "Found no constrained element with name " + executableName + " on type " + beanType );
	}

	private static class Foo {

		@SuppressWarnings("unused")
		public Foo(@NotNull String foo) {
		}
	}

	private static class Calendar {

		@ConsistentDateParameters
		public void createEvent(DateMidnight start, DateMidnight end) {
		}
	}
}
