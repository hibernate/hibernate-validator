/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.KeyboardLayout;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyRule;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;

class DefaultPasswordPolicyBuilder implements PasswordPolicyBuilder {

	private final List<PasswordPolicyRule> rules = new ArrayList<>();

	@Override
	public PasswordPolicyBuilder minLength(int min) {
		rules.add( new MinLengthRule( min ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder maxLength(int max) {
		rules.add( new MaxLengthRule( max ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder requireCharacters(CharacterType type, int min) {
		rules.add( new CharacterRule( type, min ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noSequence() {
		rules.add( new NoSequenceRule() );
		return this;
	}

	@Override
	public PasswordPolicyBuilder strengthEstimator(int minScore, PasswordStrengthEstimator estimator) {
		rules.add( new StrengthRule( minScore, estimator ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder compromisedChecker(CompromisedPasswordChecker checker) {
		rules.add( new CompromisedRule( checker ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder addRule(PasswordPolicyRule rule) {
		rules.add( rule );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noKeyboardWalk() {
		rules.add( new KeyboardWalkRule( KeyboardWalkRule.DEFAULT_MIN_WALK_LENGTH ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noKeyboardWalk(KeyboardLayout... layouts) {
		rules.add( new KeyboardWalkRule( KeyboardWalkRule.DEFAULT_MIN_WALK_LENGTH, layouts ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noKeyboardWalk(int minWalkLength, KeyboardLayout... layouts) {
		rules.add( new KeyboardWalkRule( minWalkLength, layouts ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noWhitespace() {
		rules.add( new WhitespaceRule() );
		return this;
	}

	@Override
	public PasswordPolicyBuilder noRepeatingCharacters(int maxConsecutive) {
		rules.add( new RepeatingCharactersRule( maxConsecutive ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder allowedCharacters(char... chars) {
		rules.add( new AllowedCharactersRule( chars ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder illegalCharacters(char... chars) {
		rules.add( new IllegalCharactersRule( chars ) );
		return this;
	}

	@Override
	public PasswordPolicyBuilder addRule(String message, Predicate<char[]> rule) {
		rules.add( new LambdaRule( message, rule ) );
		return this;
	}

	List<PasswordPolicyRule> build() {
		return List.copyOf( rules );
	}
}
