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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import javax.validation.ConvertGroup;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

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
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.buildHashMap;

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

		ConstrainedExecutable createEvent = findConstrainedExecutable(
				beanConfigurations,
				Calendar.class.getMethod( "createEvent", DateMidnight.class, DateMidnight.class )
		);

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

	@Test
	public void configurationsHaveAnnotationSource() {

		for ( BeanConfiguration<? super User> configuration : provider.getBeanConfigurationForHierarchy( User.class ) ) {
			assertThat( configuration.getSource() ).isEqualTo( ConfigurationSource.ANNOTATION );
		}
	}

	@Test
	public void noGroupConversionOnField() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class.getDeclaredField( "mail" ) );

		//then
		assertThat( field.getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnField() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class.getDeclaredField( "phone" ) );

		//then
		assertThat( field.getGroupConversions() ).isEqualTo(
				buildHashMap().with( Default.class, BasicNumber.class ).build()
		);
	}

	@Test
	public void multipleGroupConversionsOnField() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class.getDeclaredField( "address" ) );

		//then
		assertThat( field.getGroupConversions() ).isEqualTo(
				buildHashMap()
						.with( Default.class, BasicPostal.class )
						.with( Complete.class, FullPostal.class )
						.build()
		);
	}

	@Test
	public void noGroupConversionOnMethod() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "getMail1" )
		);

		//then
		assertThat( method.getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnMethod() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "getPhone1" )
		);

		//then
		assertThat( method.getGroupConversions() ).isEqualTo(
				buildHashMap().with( Default.class, BasicNumber.class ).build()
		);
	}

	@Test
	public void multipleGroupConversionsOnMethod() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "getAddress1" )
		);

		//then
		assertThat( method.getGroupConversions() ).isEqualTo(
				buildHashMap()
						.with( Default.class, BasicPostal.class )
						.with( Complete.class, FullPostal.class )
						.build()
		);
	}

	@Test
	public void noGroupConversionOnParameter() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "setMail1", String.class )
		);

		//then
		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnParameter() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "setPhone1", PhoneNumber.class )
		);

		//then
		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo(
				buildHashMap().with( Default.class, BasicNumber.class ).build()
		);
	}

	@Test
	public void multipleGroupConversionsOnParameter() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedExecutable(
				beanConfigurations,
				User.class.getMethod( "setAddress1", Address.class )
		);

		//then
		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo(
				buildHashMap()
						.with( Default.class, BasicPostal.class )
						.with( Complete.class, FullPostal.class )
						.build()
		);
	}

	@Test
	public void singleGroupConversionOnConstructor() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable constructor = findConstrainedExecutable(
				beanConfigurations,
				User.class.getConstructor()
		);

		//then
		assertThat( constructor.getGroupConversions() ).isEqualTo(
				buildHashMap().with( Default.class, BasicNumber.class ).build()
		);
	}

	@Test
	public void multipleGroupConversionsOnConstructorParameter() throws Exception {

		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable constructor = findConstrainedExecutable(
				beanConfigurations,
				User.class.getConstructor( Address.class )
		);

		//then
		assertThat( constructor.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo(
				buildHashMap()
						.with( Default.class, BasicPostal.class )
						.with( Complete.class, FullPostal.class )
						.build()
		);
	}

	private ConstrainedField findConstrainedField(Iterable<? extends BeanConfiguration<?>> beanConfigurations, Field field) {
		return (ConstrainedField) findConstrainedElement( beanConfigurations, field );
	}

	private ConstrainedExecutable findConstrainedExecutable(Iterable<? extends BeanConfiguration<?>> beanConfigurations, Method method) {
		return (ConstrainedExecutable) findConstrainedElement( beanConfigurations, method );
	}

	private <T> ConstrainedExecutable findConstrainedExecutable(Iterable<BeanConfiguration<? super T>> beanConfigurations, Constructor<T> constructor) {
		return (ConstrainedExecutable) findConstrainedElement( beanConfigurations, constructor );
	}

	private ConstrainedElement findConstrainedElement(Iterable<? extends BeanConfiguration<?>> beanConfigurations, Member member) {

		for ( BeanConfiguration<?> oneConfiguration : beanConfigurations ) {
			for ( ConstrainedElement constrainedElement : oneConfiguration.getConstrainedElements() ) {
				if ( constrainedElement.getLocation().getMember().equals( member ) ) {
					return constrainedElement;
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for " + member );
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

	public interface Complete extends Default {
	}

	public interface BasicPostal {
	}

	public interface FullPostal extends BasicPostal {
	}

	private interface BasicNumber {
	}

	private static class Address {
	}

	private static class PhoneNumber {
	}

	@SuppressWarnings("unused")
	private static class User {

		private final String mail = null;

		@Valid
		@ConvertGroup(from = Default.class, to = BasicNumber.class)
		private final PhoneNumber phone = null;

		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Complete.class, to = FullPostal.class)
		})
		private final Address address = null;

		@Valid
		@ConvertGroup(from = Default.class, to = BasicNumber.class)
		public User() {
		}

		public User(
				@Valid
				@ConvertGroup.List({
						@ConvertGroup(from = Default.class, to = BasicPostal.class),
						@ConvertGroup(from = Complete.class, to = FullPostal.class)
				})
				Address address) {
		}

		public String getMail1() {
			return null;
		}

		public void setMail1(String mail) {
		}

		@Valid
		@ConvertGroup(from = Default.class, to = BasicNumber.class)
		public PhoneNumber getPhone1() {
			return null;
		}

		public void setPhone1(@Valid @ConvertGroup(from = Default.class, to = BasicNumber.class) PhoneNumber phone) {
		}

		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Complete.class, to = FullPostal.class)
		})

		public Address getAddress1() {
			return null;
		}

		public void setAddress1(
				@Valid
				@ConvertGroup.List({
						@ConvertGroup(from = Default.class, to = BasicPostal.class),
						@ConvertGroup(from = Complete.class, to = FullPostal.class)
				})
				Address address) {
		}
	}
}
