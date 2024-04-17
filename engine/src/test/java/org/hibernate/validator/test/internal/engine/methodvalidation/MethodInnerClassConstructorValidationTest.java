/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MethodInnerClassConstructorValidationTest {

	protected ExecutableValidator executableValidator;
	private final CustomerRepositoryImplMetamodel metamodel = new CustomerRepositoryImplMetamodel();

	@BeforeMethod
	public void setUp() {
		this.executableValidator = getValidator().forExecutables();
	}

	@Test
	public void constructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<? extends ConstraintViolation<?>> violations = executableValidator.validateConstructorParameters(
				metamodel.clazz().getConstructor( CustomerRepositoryImplMetamodel.class, String.class ),
				new Object[] { metamodel, null }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( metamodel.clazz() )
						.withPropertyPath( pathWith()
								.constructor( metamodel.clazz() )
								.parameter( "id", 1 )
						)
		);
	}

	@Test
	public void cascadedConstructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<? extends ConstraintViolation<?>> violations = executableValidator.validateConstructorParameters(
				metamodel.clazz().getConstructor( CustomerRepositoryImplMetamodel.class, Customer.class ),
				new Object[] { metamodel, new Customer( null ) }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( metamodel.clazz() )
						.withPropertyPath( pathWith()
								.constructor( metamodel.clazz() )
								.parameter( "customer", 1 )
								.property( "name" )
						)
		);
	}

	@Test
	public void constructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		Constructor<?> constructor = metamodel.clazz().getDeclaredConstructor( CustomerRepositoryImplMetamodel.class );
		Object customerRepository = constructor.newInstance( metamodel );
		Set<? extends ConstraintViolation<?>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				customerRepository
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidB2BRepository.class )
						.withMessage( "{ValidB2BRepository.message}" )
						.withInvalidValue( customerRepository )
						.withRootBeanClass( metamodel.clazz() )
						.withPropertyPath( pathWith()
								.constructor( metamodel.clazz() )
								.returnValue()
						)
		);
	}

	@Test
	public void cascadedConstructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		Constructor<?> constructor = metamodel.clazz().getDeclaredConstructor(
				CustomerRepositoryImplMetamodel.class, String.class );
		Object customerRepository = constructor.newInstance( metamodel, null );
		Set<? extends ConstraintViolation<?>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				customerRepository
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( metamodel.clazz() )
						.withPropertyPath( pathWith()
								.constructor( metamodel.clazz() )
								.returnValue()
								.property( "customer" )
						)
		);
	}

	private static class CustomerRepositoryImplMetamodel {
		public Class<?> clazz() {
			class CustomerRepositoryImpl {
				@NotNull
				private final Customer customer = null;

				@ValidB2BRepository
				public CustomerRepositoryImpl() {
				}

				@Valid
				public CustomerRepositoryImpl(@NotNull String id) {
				}

				public CustomerRepositoryImpl(@Valid Customer customer) {
				}
			}
			return CustomerRepositoryImpl.class;
		}

		public Object newInstance()
				throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
			return clazz().getDeclaredConstructor( CustomerRepositoryImplMetamodel.class ).newInstance( this );
		}
	}

	@Constraint(validatedBy = { ValidB2BRepositoryValidator.class })
	@Target({ TYPE, CONSTRUCTOR })
	@Retention(RUNTIME)
	public @interface ValidB2BRepository {
		String message() default "{ValidB2BRepository.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidB2BRepositoryValidator
			implements ConstraintValidator<ValidB2BRepository, Object> {
		@Override
		public boolean isValid(Object repository, ConstraintValidatorContext context) {
			return false;
		}
	}
}
