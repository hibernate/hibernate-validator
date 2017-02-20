/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
		public static final String CONSTRAINT_TARGET = JAVAX_VALIDATION + ".ConstraintTarget";
		public static final String CONSTRAINT_VALIDATOR = JAVAX_VALIDATION + ".ConstraintValidator";
		public static final String GROUP_SEQUENCE = JAVAX_VALIDATION + ".GroupSequence";
		public static final String PAYLOAD = JAVAX_VALIDATION + ".Payload";
		public static final String VALID = JAVAX_VALIDATION + ".Valid";

		public static final String JAVAX_VALIDATION_CONSTRAINTS = "javax.validation.constraints";

		public static final String ASSERT_FALSE = JAVAX_VALIDATION_CONSTRAINTS + ".AssertFalse";
		public static final String ASSERT_TRUE = JAVAX_VALIDATION_CONSTRAINTS + ".AssertTrue";
		public static final String DECIMAL_MAX = JAVAX_VALIDATION_CONSTRAINTS + ".DecimalMax";
		public static final String DECIMAL_MIN = JAVAX_VALIDATION_CONSTRAINTS + ".DecimalMin";
		public static final String DIGITS = JAVAX_VALIDATION_CONSTRAINTS + ".Digits";
		public static final String FUTURE = JAVAX_VALIDATION_CONSTRAINTS + ".Future";
		public static final String MAX = JAVAX_VALIDATION_CONSTRAINTS + ".Max";
		public static final String MIN = JAVAX_VALIDATION_CONSTRAINTS + ".Min";
		public static final String NOT_NULL = JAVAX_VALIDATION_CONSTRAINTS + ".NotNull";
		public static final String NULL = JAVAX_VALIDATION_CONSTRAINTS + ".Null";
		public static final String PAST = JAVAX_VALIDATION_CONSTRAINTS + ".Past";
		public static final String PATTERN = JAVAX_VALIDATION_CONSTRAINTS + ".Pattern";
		public static final String SIZE = JAVAX_VALIDATION_CONSTRAINTS + ".Size";

		public static final String CONSTRAINTVALIDATION = "javax.validation.constraintvalidation";

		public static final String SUPPORTED_VALIDATION_TARGET = CONSTRAINTVALIDATION + ".SupportedValidationTarget";
	}

	public static class HibernateValidatorTypes {

		private static final String ORG_HIBERNATE_VALIDATOR_GROUP = "org.hibernate.validator.group";
		public static final String GROUP_SEQUENCE_PROVIDER = ORG_HIBERNATE_VALIDATOR_GROUP + ".GroupSequenceProvider";

		private static final String ORG_HIBERNATE_VALIDATOR_SPI_GROUP = "org.hibernate.validator.spi.group";
		public static final String DEFAULT_GROUP_SEQUENCE_PROVIDER = ORG_HIBERNATE_VALIDATOR_SPI_GROUP + ".DefaultGroupSequenceProvider";

		private static final String ORG_HIBERNATE_VALIDATOR_CONSTRAINTS = "org.hibernate.validator.constraints";

		public static final String CURRENCY = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Currency";
		public static final String EMAIL = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Email";
		public static final String LENGTH = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Length";
		public static final String MOD_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".ModCheck";
		public static final String LUHN_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".LuhnCheck";
		public static final String MOD_10_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Mod10Check";
		public static final String MOD_11_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".Mod11Check";
		public static final String REGON_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".pl.REGON";
		public static final String NIP_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".pl.NIP";
		public static final String PESEL_CHECK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".pl.PESEL";
		public static final String NOT_BLANK = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".NotBlank";
		public static final String SAFE_HTML = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".SafeHtml";
		public static final String SCRIPT_ASSERT = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".ScriptAssert";
		public static final String URL = ORG_HIBERNATE_VALIDATOR_CONSTRAINTS + ".URL";
	}

	public static class JodaTypes {

		private static final String ORG_JODA_TIME = "org.joda.time";

		public static final String READABLE_PARTIAL = ORG_JODA_TIME + ".ReadablePartial";
		public static final String READABLE_INSTANT = ORG_JODA_TIME + ".ReadableInstant";
	}

	public static class Java8DateTime {

		private static final String JAVA_TIME = "java.time";

		public static final String CHRONO_ZONED_DATE_TIME = JAVA_TIME + ".chrono.ChronoZonedDateTime";
		public static final String OFFSET_DATE_TIME = JAVA_TIME + ".OffsetDateTime";
		public static final String INSTANT = JAVA_TIME + ".Instant";
	}

	public static class JavaMoneyTypes {

		private static final String JAVAX_MONEY = "javax.money";

		public static final String MONETARY_AMOUNT = JAVAX_MONEY + ".MonetaryAmount";

	}

}
