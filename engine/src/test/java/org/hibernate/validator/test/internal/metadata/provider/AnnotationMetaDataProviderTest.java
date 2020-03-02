/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.provider;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.Map;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AnnotationMetaDataProvider}.
 *
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProviderTest extends AnnotationMetaDataProviderTestBase {

	private AnnotationMetaDataProvider provider;

	@BeforeMethod
	public void setUpProvider() {
		provider = new AnnotationMetaDataProvider(
				getDummyConstraintCreationContext(),
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ),
				new AnnotationProcessingOptionsImpl()
		);
	}

	@Test
	public void testGetConstructorMetaData() throws Exception {
		BeanConfiguration<Foo> beanConfiguration = provider.getBeanConfiguration( Foo.class );

		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfiguration, Foo.class, String.class );

		assertThat( constructor.getKind() ).isEqualTo( ConstrainedElementKind.CONSTRUCTOR );
		assertThat( constructor.isConstrained() ).isTrue();
		assertThat( constructor.getCascadingMetaDataBuilder().isCascading() ).isFalse();
		assertThat( constructor.getConstraints() ).hasSize( 1 );

		MetaConstraint<?> constraint = constructor.getConstraints().iterator().next();
		assertThat( constraint.getDescriptor().getAnnotation().annotationType() ).isEqualTo( NotNull.class );
		assertThat( constraint.getConstraintLocationKind() ).isEqualTo( ConstraintLocationKind.CONSTRUCTOR );
	}

	@Test
	public void testGetCrossParameterMetaData() throws Exception {
		//when
		BeanConfiguration<Calendar> beanConfiguration = provider.getBeanConfiguration( Calendar.class );

		ConstrainedExecutable createEvent = findConstrainedMethod(
				beanConfiguration,
				Calendar.class,
				"createEvent",
				LocalDate.class,
				LocalDate.class
		);

		//then
		assertThat( createEvent.isConstrained() ).isTrue();
		assertThat( createEvent.getCascadingMetaDataBuilder().isCascading() ).isFalse();
		assertThat( createEvent.getKind() ).isEqualTo( ConstrainedElementKind.METHOD );
		assertThat( createEvent.getConstraints() ).as( "No return value constraints expected" ).isEmpty();
		assertThat( createEvent.getCrossParameterConstraints() ).hasSize( 1 );

		assertThat( createEvent.getCallable() ).isEqualTo(
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() )
						.findDeclaredMethod( Calendar.class, "createEvent", LocalDate.class, LocalDate.class )
						.get()
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
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		assertThat( beanConfiguration.getSource() ).isEqualTo( ConfigurationSource.ANNOTATION );
	}

	@Test
	public void noGroupConversionOnField() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedField field = findConstrainedField( beanConfiguration, User.class, "mail" );

		//then
		assertThat( field.getCascadingMetaDataBuilder().getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnField() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedField field = findConstrainedField( beanConfiguration, User.class, "phone" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( field.getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnField() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedField field = findConstrainedField( beanConfiguration, User.class, "address" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( field.getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void multipleGroupConversionsOnFieldWithSameFromCauseException() {
		provider.getBeanConfiguration( User2.class );
	}

	@Test
	public void noGroupConversionOnMethod() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfiguration, User.class, "getMail1" );

		//then
		assertThat( method.getCascadingMetaDataBuilder().getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnMethod() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfiguration, User.class, "getPhone1" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( method.getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnMethod() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod( beanConfiguration, User.class, "getAddress1" );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( method.getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void noGroupConversionOnParameter() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfiguration,
				User.class,
				"setMail1",
				String.class
		);

		//then
		assertThat( method.getParameterMetaData( 0 ).getCascadingMetaDataBuilder().getGroupConversions() ).isEmpty();
	}

	@Test
	public void singleGroupConversionOnParameter() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfiguration,
				User.class,
				"setPhone1",
				PhoneNumber.class
		);

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( method.getParameterMetaData( 0 ).getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnParameter() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable method = findConstrainedMethod(
				beanConfiguration,
				User.class,
				"setAddress1",
				Address.class
		);

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( method.getParameterMetaData( 0 ).getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void multipleGroupConversionsOnParameterWithSameFromCauseException() {
		provider.getBeanConfiguration( User4.class );
	}

	@Test
	public void singleGroupConversionOnConstructor() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfiguration, User.class );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicNumber.class );

		assertThat( constructor.getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	public void multipleGroupConversionsOnConstructorParameter() throws Exception {
		//when
		BeanConfiguration<User> beanConfiguration = provider.getBeanConfiguration( User.class );
		ConstrainedExecutable constructor = findConstrainedConstructor( beanConfiguration, User.class, Address.class );

		//then
		Map<Class<?>, Class<?>> expected = newHashMap();
		expected.put( Default.class, BasicPostal.class );
		expected.put( Complete.class, FullPostal.class );

		assertThat( constructor.getParameterMetaData( 0 ).getCascadingMetaDataBuilder().getGroupConversions() ).isEqualTo( expected );
	}

	@Test
	@TestForIssue(jiraKey = "HV-626")
	public void onlyLocallyDefinedConstraintsAreConsidered() {
		BeanConfiguration<Person> beanConfiguration = provider.getBeanConfiguration( Person.class );

		ConstrainedType personType = findConstrainedType( beanConfiguration, Person.class );
		assertThat( personType.getConstraints() ).hasSize( 1 );
		ConstraintDescriptor<?> constraintInSubType = personType.getConstraints()
				.iterator()
				.next()
				.getDescriptor();
		assertThat( constraintInSubType.getAnnotation().annotationType() ).isEqualTo( ScriptAssert.class );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000124.*")
	public void groupConversionWithSameFromInSingleAndListAnnotationCauseException() {
		provider.getBeanConfiguration( User3.class );
	}

	private static class Foo {
		@NotNull
		public Foo(@NotNull String foo) {
		}
	}

	private static class Calendar {
		@ConsistentDateParameters
		public void createEvent(LocalDate start, LocalDate end) {
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

}
