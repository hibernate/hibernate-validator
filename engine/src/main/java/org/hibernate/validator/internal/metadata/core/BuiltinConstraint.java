/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The list of built-in constraints.
 * <p>
 * We are not using the class directly to avoid loading the class if possible.
 * <p>
 * In the case of composing constraints, you need to define the dependencies manually.
 * <p>
 * When adding a new built-in constraint, please add a test in {@code MessagePropertiesTest} and
 * {@code PredefinedScopeAllConstraintsTest}.
 *
 * @author Guillaume Smet
 */
enum BuiltinConstraint {

	// Specification constraints
	JAKARTA_VALIDATION_CONSTRAINTS_ASSERT_FALSE("jakarta.validation.constraints.AssertFalse"),
	JAKARTA_VALIDATION_CONSTRAINTS_ASSERT_TRUE("jakarta.validation.constraints.AssertTrue"),
	JAKARTA_VALIDATION_CONSTRAINTS_DECIMAL_MAX("jakarta.validation.constraints.DecimalMax"),
	JAKARTA_VALIDATION_CONSTRAINTS_DECIMAL_MIN("jakarta.validation.constraints.DecimalMin"),
	JAKARTA_VALIDATION_CONSTRAINTS_DIGITS("jakarta.validation.constraints.Digits"),
	JAKARTA_VALIDATION_CONSTRAINTS_EMAIL("jakarta.validation.constraints.Email"),
	JAKARTA_VALIDATION_CONSTRAINTS_FUTURE("jakarta.validation.constraints.Future"),
	JAKARTA_VALIDATION_CONSTRAINTS_FUTURE_OR_PRESENT("jakarta.validation.constraints.FutureOrPresent"),
	JAKARTA_VALIDATION_CONSTRAINTS_MIN("jakarta.validation.constraints.Min"),
	JAKARTA_VALIDATION_CONSTRAINTS_MAX("jakarta.validation.constraints.Max"),
	JAKARTA_VALIDATION_CONSTRAINTS_NEGATIVE("jakarta.validation.constraints.Negative"),
	JAKARTA_VALIDATION_CONSTRAINTS_NEGATIVE_OR_ZERO("jakarta.validation.constraints.NegativeOrZero"),
	JAKARTA_VALIDATION_CONSTRAINTS_NOT_BLANK("jakarta.validation.constraints.NotBlank"),
	JAKARTA_VALIDATION_CONSTRAINTS_NOT_EMPTY("jakarta.validation.constraints.NotEmpty"),
	JAKARTA_VALIDATION_CONSTRAINTS_NOT_NULL("jakarta.validation.constraints.NotNull"),
	JAKARTA_VALIDATION_CONSTRAINTS_NULL("jakarta.validation.constraints.Null"),
	JAKARTA_VALIDATION_CONSTRAINTS_PAST("jakarta.validation.constraints.Past"),
	JAKARTA_VALIDATION_CONSTRAINTS_PAST_OR_PRESENT("jakarta.validation.constraints.PastOrPresent"),
	JAKARTA_VALIDATION_CONSTRAINTS_PATTERN("jakarta.validation.constraints.Pattern"),
	JAKARTA_VALIDATION_CONSTRAINTS_POSITIVE("jakarta.validation.constraints.Positive"),
	JAKARTA_VALIDATION_CONSTRAINTS_POSITIVE_OR_ZERO("jakarta.validation.constraints.PositiveOrZero"),
	JAKARTA_VALIDATION_CONSTRAINTS_SIZE("jakarta.validation.constraints.Size"),

	// Hibernate Validator specific constraints
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_CODE_POINT_LENGTH("org.hibernate.validator.constraints.CodePointLength"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_CURRENCY("org.hibernate.validator.constraints.Currency"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_EMAIL("org.hibernate.validator.constraints.Email", Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_PATTERN ) ),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_ISBN("org.hibernate.validator.constraints.ISBN"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_LENGTH("org.hibernate.validator.constraints.Length"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_LUHN_CHECK("org.hibernate.validator.constraints.LuhnCheck"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_CREDIT_CARD_NUMBER("org.hibernate.validator.constraints.CreditCardNumber",
			Arrays.asList( ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_LUHN_CHECK ) ),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_MOD10_CHECK("org.hibernate.validator.constraints.Mod10Check"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_MOD11_CHECK("org.hibernate.validator.constraints.Mod11Check"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_MOD_CHECK("org.hibernate.validator.constraints.ModCheck"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_NORMALIZED("org.hibernate.validator.constraints.Normalized"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_EAN("org.hibernate.validator.constraints.EAN", Arrays.asList( ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_MOD10_CHECK ) ),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_NOT_BLANK("org.hibernate.validator.constraints.NotBlank",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_NOT_NULL ) ),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_NOT_EMPTY("org.hibernate.validator.constraints.NotEmpty",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_NOT_NULL, JAKARTA_VALIDATION_CONSTRAINTS_SIZE )),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_PARAMETER_SCRIPT_ASSERT("org.hibernate.validator.constraints.ParameterScriptAssert"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_RANGE("org.hibernate.validator.constraints.Range",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_MIN, JAKARTA_VALIDATION_CONSTRAINTS_MAX )),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_SCRIPT_ASSERT("org.hibernate.validator.constraints.ScriptAssert"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_URL("org.hibernate.validator.constraints.URL",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_PATTERN )),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_UNIQUE_ELEMENTS("org.hibernate.validator.constraints.UniqueElements"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_BR_CNPJ("org.hibernate.validator.constraints.br.CNPJ",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_PATTERN )),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_BR_CPF("org.hibernate.validator.constraints.br.CPF",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_PATTERN )),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_BR_TITULO_ELEITORAL("org.hibernate.validator.constraints.br.TituloEleitoral",
			Arrays.asList( JAKARTA_VALIDATION_CONSTRAINTS_PATTERN, ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_MOD11_CHECK ) ),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_PL_NIP("org.hibernate.validator.constraints.pl.NIP"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_PL_PESEL("org.hibernate.validator.constraints.pl.PESEL"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_PL_REGON("org.hibernate.validator.constraints.pl.REGON"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_RU_INN("org.hibernate.validator.constraints.ru.INN"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_TIME_DURATION_MAX("org.hibernate.validator.constraints.time.DurationMax"),
	ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_TIME_DURATION_MIN("org.hibernate.validator.constraints.time.DurationMin");

	private static final Map<String, Set<BuiltinConstraint>> CONSTRAINT_MAPPING;

	static {
		CONSTRAINT_MAPPING = new HashMap<>();
		for ( BuiltinConstraint constraint : values() ) {
			if ( constraint.constraintDependencies.isEmpty() ) {
				CONSTRAINT_MAPPING.put( constraint.annotationClassName, Collections.singleton( constraint ) );
			}
			else {
				Set<BuiltinConstraint> constraints = new HashSet<>();
				constraints.add( constraint );
				constraints.addAll( constraint.constraintDependencies );
				CONSTRAINT_MAPPING.put( constraint.annotationClassName, constraints );
			}
		}
	}

	private String annotationClassName;
	private List<BuiltinConstraint> constraintDependencies;

	BuiltinConstraint(String constraint) {
		this( constraint, Collections.emptyList() );
	}

	BuiltinConstraint(String constraint, List<BuiltinConstraint> composingConstraints) {
		this.annotationClassName = constraint;
		this.constraintDependencies = composingConstraints;
	}

	static Set<BuiltinConstraint> resolve(Set<String> constraints) {
		Set<BuiltinConstraint> resolvedConstraints = new HashSet<>();
		for ( String constraint : constraints ) {
			Set<BuiltinConstraint> builtinConstraints = CONSTRAINT_MAPPING.get( constraint );
			if ( builtinConstraints != null ) {
				resolvedConstraints.addAll( builtinConstraints );
			}
		}
		return resolvedConstraints;
	}

	static boolean isBuiltin(String constraint) {
		return CONSTRAINT_MAPPING.containsKey( constraint );
	}

	static Set<String> set() {
		return CONSTRAINT_MAPPING.keySet();
	}
}
