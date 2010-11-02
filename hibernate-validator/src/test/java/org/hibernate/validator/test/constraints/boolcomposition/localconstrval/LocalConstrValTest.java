/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import org.testng.annotations.Test;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintTypes;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectPropertyPaths;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;

/**
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class LocalConstrValTest {

	/**
	 * HV-390
	 * Used to test whether boolean composition works with local ConstraintValidators
	 */
	@Test
	public void testCorrectBooleanEvaluation() {
		Validator currentValidator = TestUtil.getValidator();
        
		//nothing should fail, the pattern mathces on name
		Set<ConstraintViolation<PersonConstrVal>> constraintViolations = currentValidator.validate(
				new PersonConstrVal( "6chars", "WWWW" )
		);

		assertNumberOfViolations( constraintViolations, 0 );
		
        //nickname is too long
		constraintViolations = currentValidator.validate(
				new PersonConstrVal(
						"12characters", "loongstring"
				)
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, SmallString.class);
		assertCorrectPropertyPaths(constraintViolations, "nickName");
		
		//nickName fails for violating @Size, but is reported as SingleViolation
		//name fails for violating both Pattern and the test in LongStringValidator. In a way it is reported 
		//both as single violation and as multiple violations (weird).
		constraintViolations = currentValidator.validate(
				new PersonConstrVal(
						"exactlyTEN", "tinystr"
				)
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectConstraintTypes( constraintViolations, SmallString.class,Pattern.class,PatternOrLong.class);
		assertCorrectPropertyPaths(constraintViolations, "nickName","name","name");
	
		
	}
}
