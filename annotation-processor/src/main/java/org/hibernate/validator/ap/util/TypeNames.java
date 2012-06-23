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
package org.hibernate.validator.ap.util;

/**
 * Contains the fully-qualified names of types used by the annotation processor
 * which are accessed only via the mirror API in order to avoid references to
 * the actual class objects.
 *
 * @author Gunnar Morling
 */
public class TypeNames {

	public static class BeanValidationTypes {

		public static final String JAVAX_VALIDATION = "javax.validation";

		public static final String CONSTRAINT = JAVAX_VALIDATION + ".Constraint";
		public static final String CONSTRAINT_VALIDATOR = JAVAX_VALIDATION + ".ConstraintValidator";
		public static final String GROUP_SEQUENCE = JAVAX_VALIDATION + ".GroupSequence";
		public static final String PAYLOAD = JAVAX_VALIDATION + ".Payload";
		public static final String VALID = JAVAX_VALIDATION + ".Valid";

		public static final String JAVAX_VALIDATION_CONSTRAINTS = "javax.validation.constraints";

		public final static String ASSERT_FALSE = JAVAX_VALIDATION_CONSTRAINTS + ".AssertFalse";
		public final static String ASSERT_TRUE = JAVAX_VALIDATION_CONSTRAINTS + ".AssertTrue";
		public final static String DECIMAL_MAX = JAVAX_VALIDATION_CONSTRAINTS + ".DecimalMax";
		public final static String DECIMAL_MIN = JAVAX_VALIDATION_CONSTRAINTS + ".DecimalMin";
		public final static String DIGITS = JAVAX_VALIDATION_CONSTRAINTS + ".Digits";
		public final static String FUTURE = JAVAX_VALIDATION_CONSTRAINTS + ".Future";
		public final static String MAX = JAVAX_VALIDATION_CONSTRAINTS + ".Max";
		public final static String MIN = JAVAX_VALIDATION_CONSTRAINTS + ".Min";
		public final static String NOT_NULL = JAVAX_VALIDATION_CONSTRAINTS + ".NotNull";
		public final static String NULL = JAVAX_VALIDATION_CONSTRAINTS + ".Null";
		public final static String PAST = JAVAX_VALIDATION_CONSTRAINTS + ".Past";
		public final static String PATTERN = JAVAX_VALIDATION_CONSTRAINTS + ".Pattern";
		public final static String SIZE = JAVAX_VALIDATION_CONSTRAINTS + ".Size";
	}

	public static class HibernateValidatorTypes {

		private static final String ORG_HIBERNATE_VALIDATOR_GROUP = "org.hibernate.validator.group";
		public final static String GROUP_SEQUENCE_PROVIDER = ORG_HIBERNATE_VALIDATOR_GROUP + ".GroupSequenceProvider";

		private static final String ORG_HIBERNATE_VALIDATOR_SPI_GROUP = "org.hibernate.validator.spi.group";
		public final static String DEFAULT_GROUP_SEQUENCE_PROVIDER = ORG_HIBERNATE_VALIDATOR_SPI_GROUP + ".DefaultGroupSequenceProvider";

		private static final String ORG_HIBERNATE_VALIDATOR_CONSTRAINTS = "org.hibernate.validator.constraints";

		public final static String EMAIL = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Email";
		public final static String LENGTH = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Length";
		public final static String MOD_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".ModCheck";
		public final static String NOT_BLANK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".NotBlank";
		public final static String SAFE_HTML = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".SafeHtml";
		public final static String SCRIPT_ASSERT = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".ScriptAssert";
		public final static String URL = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".URL";
	}

	public static class JodaTypes {

		private static final String ORG_JODA_TIME = "org.joda.time";

		public final static String READABLE_PARTIAL = ORG_JODA_TIME + ".ReadablePartial";
		public final static String READABLE_INSTANT = ORG_JODA_TIME + ".ReadableInstant";
	}
}
