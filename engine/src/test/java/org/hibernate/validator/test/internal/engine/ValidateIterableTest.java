/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Hardy Ferentschik
 * @author Adam Stawicki
 */
@TestForIssue(jiraKey = "HV-902")
public class ValidateIterableTest {
    private static class TestNotNullValidation implements Iterable<Object> {
        @NotNull
        private Integer cannotBeNull = null;

        @Override
        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }
    }

    private static class TestCascadingValidation {
        @Valid
        private TestNotNullValidation cannotHaveNull = new TestNotNullValidation();
    }

    @Test
    public void testCascadingValidationWhenCascadedTypeImplementsIterable() {
        TestCascadingValidation validateThis = new TestCascadingValidation();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<TestCascadingValidation>> violations = validator.validate(validateThis);
        assertThat(violations).isNotEmpty();
    }
}
