/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.cdi.internal.util.BuiltInConstraintValidatorUtils.isBuiltInConstraintValidator;

import java.lang.annotation.Annotation;
import java.util.Random;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.engine.HibernateValidatorEnhancedBean;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;

import org.testng.annotations.Test;

public class BuiltInConstraintValidatorUtilsTest {

	@Test
	public void testAllActualBuiltInPassCheck() throws ClassNotFoundException {
		ConstraintHelper helper = ConstraintHelper.forAllBuiltinConstraints();

		for ( String builtinConstraint : ConstraintHelper.getBuiltinConstraints() ) {
			Class<? extends Annotation> constraint = (Class<? extends Annotation>) Class.forName( builtinConstraint );
			for ( ConstraintValidatorDescriptor<? extends Annotation> descriptor : helper.getAllValidatorDescriptors( constraint ) ) {
				assertThat( isBuiltInConstraintValidator( descriptor.getValidatorClass() ) ).isTrue();
			}
		}
	}

	@Test
	public void testSomeRandomClasses() {
		assertThat( isBuiltInConstraintValidator( Random.class ) ).isFalse();
		assertThat( isBuiltInConstraintValidator( HibernateValidator.class ) ).isFalse();
		assertThat( isBuiltInConstraintValidator( HibernateValidatorFactory.class ) ).isFalse();
		assertThat( isBuiltInConstraintValidator( HibernateValidatorEnhancedBean.class ) ).isFalse();
	}
}
