package org.hibernate.validator.referenceguide.chapter12.relaxation;

import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Test;

public class RelaxationTest {

	@Test
	public void testRelaxation() {
		//tag::testRelaxation[]
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();

		configuration.allowMultipleCascadedValidationOnReturnValues( true )
				.allowOverridingMethodAlterParameterConstraint( true )
				.allowParallelMethodsDefineParameterConstraints( true );
		//end::testRelaxation[]
	}

}
