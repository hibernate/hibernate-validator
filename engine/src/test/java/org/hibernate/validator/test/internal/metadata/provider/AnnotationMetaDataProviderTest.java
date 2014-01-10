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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import javax.validation.Constraint;
import javax.validation.ConstraintDeclarationException;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
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
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.testng.Assert.assertTrue;

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
				new AnnotationProcessingOptionsImpl()
		);
	}

	@Test
	public void testGetConstructorMetaData() throws Exception {
		List<BeanConfiguration<? super Foo>> beanConfigurations = provider.getBeanConfigurationForHierarchy( Foo.class );

		assertThat( beanConfigurations ).hasSize( 2 );

		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfigurations, Foo.class, String.class );

		assertThat( constructor.getKind() ).isEqualTo( ConstrainedElementKind.CONSTRUCTOR );
		assertThat( constructor.isConstrained() ).isTrue();
		assertThat( constructor.isCascading() ).isFalse();
		assertThat( constructor.getConstraints() ).hasSize( 1 );

		MetaConstraint<?> constraint = constructor.getConstraints().iterator().next();
		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo( NotNull.class );
		assertThat( constraint.getElementType() ).isEqualTo( ElementType.CONSTRUCTOR );
	}

	@Test
	public void testGetCrossParameterMetaData() throws Exception {
		//when
		List<BeanConfiguration<? super Calendar>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Calendar.class
		);

		ConstrainedExecutable createEvent = findConstrainedMethod(
				beanConfigurations,
				Calendar.class,
				"createEvent",
				DateMidnight.class,
				DateMidnight.class
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

		MetaConstraint<?> constraint = createEvent.getCrossParameterConstraints().iterator().next();

		assertThat(
				constraint.getDescriptor()
						.getAnnotation()
						.annotationType()
		).isEqualTo( ConsistentDateParameters.class );
		assertThat( constraint.getLocation().getTypeForValidatorResolution() ).isEqualTo( Object[].class );
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
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class, "mail" );

		//then
		assertThat( field.getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnField() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class, "phone" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( field.getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnField() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedField field = findConstrainedField( beanConfigurations, User.class, "address" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( field.getGroupConversions() ).isEqualTo( expected );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void multipleGroupConversionsOnFieldWithSameFromCauseException() {
		provider.getBeanConfigurationForHierarchy( User2.class );
	}

	@Test
	public void noGroupConversionOnMethod() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfigurations, User.class, "getMail1" );

		//then
		assertThat( method.getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnMethod() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfigurations, User.class, "getPhone1" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( method.getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnMethod() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfigurations, User.class, "getAddress1" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( method.getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void noGroupConversionOnParameter() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfigurations,
				User.class,
				"setMail1",
				String.class
		);

		//then
		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnParameter() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfigurations,
				User.class,
				"setPhone1",
				PhoneNumber.class
		);

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnParameter() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfigurations,
				User.class,
				"setAddress1",
				Address.class
		);

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( method.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo( expected );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void multipleGroupConversionsOnParameterWithSameFromCauseException() {
		provider.getBeanConfigurationForHierarchy( User4.class );
	}

	@Test
	public void singleGroupConversionOnConstructor() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfigurations, User.class );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( constructor.getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnConstructorParameter() throws Exception {
		//when
		List<BeanConfiguration<? super User>> beanConfigurations = provider.getBeanConfigurationForHierarchy( User.class );
		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfigurations, User.class, Address.class );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( constructor.getParameterMetaData( 0 ).getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	@TestForIssue(jiraKey = "HV-626")
	public void onlyLocallyDefinedConstraintsAreConsidered() {
		List<BeanConfiguration<? super Person>> beanConfigurations = provider.getBeanConfigurationForHierarchy( Person.class );

		ConstrainedType personType = findConstrainedType( beanConfigurations, Person.class );
		assertThat( personType.getConstraints() ).hasSize( 1 );
		ConstraintDescriptor<?> constraintInSubType = personType.getConstraints()
				.iterator()
				.next()
				.getDescriptor();
		assertThat( constraintInSubType.getAnnotation().annotationType() ).isEqualTo( ScriptAssert.class );

		ConstrainedType personBaseType = findConstrainedType( beanConfigurations, PersonBase.class );
		assertThat( personBaseType.getConstraints() ).hasSize( 1 );

		ConstraintDescriptor<?> constraintInSuperType = personBaseType.getConstraints()
				.iterator()
				.next()
				.getDescriptor();
		assertThat( constraintInSuperType.getAnnotation().annotationType() ).isEqualTo( ClassLevelConstraint.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void groupConversionWithSameFromInSingleAndListAnnotationCauseException() {
		provider.getBeanConfigurationForHierarchy( User3.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-819")
	public void unwrapValidatedValueOnField() throws Exception {
		List<BeanConfiguration<? super GolfPlayer>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				GolfPlayer.class
		);

		ConstrainedField constrainedField = findConstrainedField( beanConfigurations, GolfPlayer.class, "name" );

		assertTrue( constrainedField.requiresUnwrapping() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-819")
	public void unwrapValidatedValueOnProperty() throws Exception {
		List<BeanConfiguration<? super GolfPlayer>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				GolfPlayer.class
		);

		ConstrainedExecutable constrainedMethod = findConstrainedMethod(
				beanConfigurations,
				GolfPlayer.class,
				"getHandicap"
		);

		assertTrue( constrainedMethod.requiresUnwrapping() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-819")
	public void unwrapValidatedValueOnMethod() throws Exception {
		List<BeanConfiguration<? super GolfPlayer>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				GolfPlayer.class
		);

		ConstrainedExecutable constrainedMethod = findConstrainedMethod(
				beanConfigurations,
				GolfPlayer.class,
				"enterTournament"
		);

		assertTrue( constrainedMethod.requiresUnwrapping() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-819")
	public void unwrapValidatedValueOnConstructor() throws Exception {
		@SuppressWarnings("rawtypes")
		List<BeanConfiguration<? super Wrapper>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				Wrapper.class
		);

		ConstrainedExecutable constrainedConstructor = findConstrainedConstructor(
				beanConfigurations,
				Wrapper.class,
				Object.class
		);

		assertTrue( constrainedConstructor.requiresUnwrapping() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-819")
	public void unwrapValidatedValueOnParameter() throws Exception {
		List<BeanConfiguration<? super GolfPlayer>> beanConfigurations = provider.getBeanConfigurationForHierarchy(
				GolfPlayer.class
		);

		ConstrainedExecutable constrainedMethod = findConstrainedMethod(
				beanConfigurations,
				GolfPlayer.class,
				"practice",
				Wrapper.class
		);

		assertTrue( constrainedMethod.getParameterMetaData( 0 ).requiresUnwrapping() );
	}

	private <T> ConstrainedField findConstrainedField(Iterable<BeanConfiguration<? super T>> beanConfigurations,
			Class<? super T> clazz, String fieldName) throws Exception {
		return (ConstrainedField) findConstrainedElement( beanConfigurations, clazz.getDeclaredField( fieldName ) );
	}

	private <T> ConstrainedExecutable findConstrainedMethod(Iterable<BeanConfiguration<? super T>> beanConfigurations,
			Class<? super T> clazz, String methodName, Class<?>... parameterTypes) throws Exception {
		return (ConstrainedExecutable) findConstrainedElement(
				beanConfigurations,
				clazz.getMethod( methodName, parameterTypes )
		);
	}

	private <T> ConstrainedExecutable findConstrainedConstructor(
			Iterable<BeanConfiguration<? super T>> beanConfigurations, Class<? super T> clazz,
			Class<?>... parameterTypes) throws Exception {
		return (ConstrainedExecutable) findConstrainedElement(
				beanConfigurations,
				clazz.getConstructor( parameterTypes )
		);
	}

	private <T> ConstrainedType findConstrainedType(Iterable<BeanConfiguration<? super T>> beanConfigurations,
			Class<? super T> type) {
		for ( BeanConfiguration<?> oneConfiguration : beanConfigurations ) {
			for ( ConstrainedElement constrainedElement : oneConfiguration.getConstrainedElements() ) {
				if ( constrainedElement.getLocation().getMember() == null ) {
					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					if ( constrainedType.getLocation().getDeclaringClass().equals( type ) ) {
						return constrainedType;
					}
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for type " + type );
	}

	private ConstrainedElement findConstrainedElement(Iterable<? extends BeanConfiguration<?>> beanConfigurations,
			Member member) {
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
		@NotNull
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

	private static class User2 {

		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Default.class, to = FullPostal.class)
		})
		private final Address address = null;
	}

	private static class User3 {

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		@ConvertGroup.List(@ConvertGroup(from = Default.class, to = FullPostal.class))
		private final Address address = null;
	}

	private static class User4 {

		@SuppressWarnings("unused")
		public void setAddress(
				@Valid
				@ConvertGroup.List({
						@ConvertGroup(from = Default.class, to = BasicPostal.class),
						@ConvertGroup(from = Default.class, to = FullPostal.class)
				})
				Address address) {
		}
	}

	@ClassLevelConstraint("some script")
	private static class PersonBase {
	}

	@ScriptAssert(lang = "javascript", script = "some script")
	private static class Person extends PersonBase {
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { })
	@Documented
	@Inherited
	public @interface ClassLevelConstraint {

		String message() default "{ClassLevelConstraint.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		String value();
	}

	private static class GolfPlayer {

		@UnwrapValidatedValue
		private Wrapper<String> name;

		@UnwrapValidatedValue
		public Wrapper<Double> getHandicap() {
			return null;
		}

		@UnwrapValidatedValue
		public Wrapper<Boolean> enterTournament() {
			return null;
		}

		@SuppressWarnings("unused")
		public void practice(@UnwrapValidatedValue Wrapper<Integer> numberOfBalls) {
		}
	}

	private static class Wrapper<T> {
		@SuppressWarnings("unused")
		public T value;

		@UnwrapValidatedValue
		public Wrapper(T value) {
			this.value = value;
		}
	}
}
