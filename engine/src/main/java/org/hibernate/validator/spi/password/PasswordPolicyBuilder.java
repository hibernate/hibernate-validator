/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import java.util.function.Predicate;

import org.hibernate.validator.Incubating;

/**
 * Builder for configuring password policy rules within a {@link PasswordPolicyDefinition}.
 * <p>
 * Provides convenience methods for common built-in rules as well as extension points
 * for custom rules via {@link #addRule(PasswordPolicyRule)} and {@link #addRule(String, Predicate)}.
 *
 * @since 9.2.0
 * @see PasswordPolicyDefinition#configure(PasswordPolicyBuilder, org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext)
 */
@Incubating
public interface PasswordPolicyBuilder {

	/**
	 * Requires the password to be at least {@code min} characters long.
	 */
	PasswordPolicyBuilder minLength(int min);

	/**
	 * Requires the password to be at most {@code max} characters long.
	 */
	PasswordPolicyBuilder maxLength(int max);

	/**
	 * Requires the password to contain at least {@code min} characters of the given type.
	 */
	PasswordPolicyBuilder requireCharacters(CharacterType type, int min);

	/**
	 * Rejects passwords that contain sequential characters (e.g. "abc", "321").
	 */
	PasswordPolicyBuilder noSequence();

	/**
	 * Requires the password to meet a minimum strength score as determined by the given estimator.
	 *
	 * @param minScore the minimum score (the scale depends on the estimator implementation)
	 * @param estimator the strength estimator to use
	 */
	PasswordPolicyBuilder strengthEstimator(int minScore, PasswordStrengthEstimator estimator);

	/**
	 * Rejects passwords that have been compromised in a data breach, as determined by the given checker.
	 *
	 * @param checker the compromised password checker to use
	 */
	PasswordPolicyBuilder compromisedChecker(CompromisedPasswordChecker checker);

	/**
	 * Rejects passwords that contain keyboard walks — sequences of characters typed
	 * by walking across physically adjacent keys (e.g. "qwerty", "4eszxdr5", "%rDxCfT6").
	 * Shifted characters are mapped to their base key.
	 * <p>
	 * Uses the {@link KeyboardLayout#QWERTY QWERTY} layout and a default minimum walk
	 * length of 4 adjacent keys.
	 */
	PasswordPolicyBuilder noKeyboardWalk();

	/**
	 * Rejects passwords that contain keyboard walks on the specified layouts.
	 * Uses a default minimum walk length of 4 adjacent keys.
	 *
	 * @param layouts the keyboard layouts to check against
	 * @see KeyboardLayout
	 */
	PasswordPolicyBuilder noKeyboardWalk(KeyboardLayout... layouts);

	/**
	 * Rejects passwords that contain keyboard walks of at least {@code minWalkLength}
	 * adjacent keys on the specified layouts. If no layouts are provided,
	 * {@link KeyboardLayout#QWERTY QWERTY} is used.
	 *
	 * @param minWalkLength the minimum number of adjacent keys to consider a walk
	 * @param layouts the keyboard layouts to check against
	 * @see KeyboardLayout
	 */
	PasswordPolicyBuilder noKeyboardWalk(int minWalkLength, KeyboardLayout... layouts);

	/**
	 * Rejects passwords that contain whitespace characters.
	 */
	PasswordPolicyBuilder noWhitespace();

	/**
	 * Rejects passwords that contain more than {@code maxConsecutive} consecutive identical characters.
	 *
	 * @param maxConsecutive the maximum number of consecutive identical characters allowed
	 */
	PasswordPolicyBuilder noRepeatingCharacters(int maxConsecutive);

	/**
	 * Restricts the password to contain only the specified characters.
	 *
	 * @param chars the set of allowed characters
	 */
	PasswordPolicyBuilder allowedCharacters(char... chars);

	/**
	 * Rejects passwords that contain any of the specified characters.
	 *
	 * @param chars the set of illegal characters
	 */
	PasswordPolicyBuilder illegalCharacters(char... chars);

	/**
	 * Adds a simple custom rule defined as a predicate with a message.
	 *
	 * @param message the violation message template
	 * @param rule a predicate that returns {@code true} if the password is valid
	 */
	PasswordPolicyBuilder addRule(String message, Predicate<char[]> rule);

	/**
	 * Adds a custom rule implemented via the {@link PasswordPolicyRule} SPI.
	 */
	PasswordPolicyBuilder addRule(PasswordPolicyRule rule);
}
