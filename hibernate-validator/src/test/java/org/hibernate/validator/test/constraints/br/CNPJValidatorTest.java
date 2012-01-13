/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraints.br;

import java.util.Set;
import javax.validation.ConstraintViolation;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

public class CNPJValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "91.509.901/0001-69" )
		);
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "ABCDEF" )
		);
		assertNumberOfViolations( violations, 1 );
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}
	}
}
