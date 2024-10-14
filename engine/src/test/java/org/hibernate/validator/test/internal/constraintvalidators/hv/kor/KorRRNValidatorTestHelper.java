/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;

import org.hibernate.validator.constraints.kor.KorRRN;
import org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit;
import org.hibernate.validator.internal.constraintvalidators.hv.kor.KorRRNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

/**
 * Test helper for provide {@link ValidateCheckDigit} into {@link KorRRNValidator}.
 *
 * @author Taewoo Kim
 */
public abstract class KorRRNValidatorTestHelper {
	protected KorRRN initAnnotation(ValidateCheckDigit validateCheckDigit) {
		ConstraintAnnotationDescriptor.Builder<KorRRN> constraintAnnotationDescriptor = new ConstraintAnnotationDescriptor.Builder<>( KorRRN.class );
		constraintAnnotationDescriptor.setAttribute( "validateCheckDigit", validateCheckDigit );
		return constraintAnnotationDescriptor.build().getAnnotation();
	}
}
