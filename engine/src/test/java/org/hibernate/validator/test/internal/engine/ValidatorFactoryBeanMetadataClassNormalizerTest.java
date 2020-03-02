/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

import org.testng.annotations.Test;

/**
 * Test for {@link ValidatorFactoryImpl}.
 *
 * @author Gunnar Morling
 */
public class ValidatorFactoryBeanMetadataClassNormalizerTest {

	@Test(expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = ".*No suitable value extractor found for type interface java.util.List.*")
	public void testBeanMetaDataClassNormalizerNoNormalizer() throws NoSuchMethodException {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		// As the proxy defines invalid constraints (see BeanProxy), we expect this to fail
		validator.forExecutables().validateParameters(
				new BeanProxy(), BeanProxy.class.getMethod( "setEmails", List.class ),
				new Object[] { Arrays.asList( "notAnEmail" ) }
		);
	}

	@Test
	public void testBeanMetaDataClassNormalizer() throws NoSuchMethodException {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.beanMetaDataClassNormalizer( new MyProxyInterfaceBeanMetaDataClassNormalizer() )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.forExecutables().validateParameters(
				new BeanProxy(), BeanProxy.class.getMethod( "setEmails", List.class ),
				new Object[] { Arrays.asList( "notAnEmail" ) }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class ).withPropertyPath(
						pathWith().method( "setEmails" )
						.parameter( "emails", 0 )
						.containerElement( "<list element>", true, null, 0, List.class, 0 )
				)
		);
	}

	private static class Bean {

		private List<String> emails;

		public Bean() {
		}

		public Bean(List<String> emails) {
			this.emails = emails;
		}

		public List<String> getEmails() {
			return emails;
		}

		public void setEmails(@Email(payload = Unwrapping.Unwrap.class) List<String> emails) {
			this.emails = emails;
		}
	}

	private interface MyProxyInterface {
	}

	private static class MyProxyInterfaceBeanMetaDataClassNormalizer implements BeanMetaDataClassNormalizer {

		@Override
		public <T> Class<? super T> normalize(Class<T> beanClass) {
			if ( MyProxyInterface.class.isAssignableFrom( beanClass ) ) {
				return beanClass.getSuperclass();
			}

			return beanClass;
		}
	}

	private static class BeanProxy extends Bean implements MyProxyInterface {
		// The proxy dropped the generics, but kept constraint annotations,
		// which will cause trouble unless its metadata is ignored.
		@Override
		@SuppressWarnings("unchecked")
		public void setEmails(@Email(payload = Unwrapping.Unwrap.class) List emails) {
			super.setEmails( emails );
		}
	}
}
