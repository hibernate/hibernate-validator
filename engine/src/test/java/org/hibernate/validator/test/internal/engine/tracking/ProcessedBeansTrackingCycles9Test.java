/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * With some raw types
 */
public class ProcessedBeansTrackingCycles9Test {

	private static Stream<Arguments> createValidators() {
		return Stream.of(
				Arguments.of( ValidatorUtil.getValidator() ),
				Arguments.of( ValidatorUtil.getPredefinedValidator( Set.of( Team.class, Player.class ) ) )
		);
	}

	@ParameterizedTest
	@MethodSource("createValidators")
	public void test(Validator validator) {
		Team team = new Team();
		Player player1 = new Player( team );
		Player player2 = new Player( team );

		team.players = List.of( player1, player2 );
		assertThat( validator.validate( team ) ).isNotEmpty();
	}

	private static class Team<P extends Player> {
		@NotNull
		String teamName;

		@Valid
		List<? extends Player> players;
	}

	private static class Player<T extends Team> {
		@Valid
		T team;

		Player(T team) {
			this.team = team;
		}
	}
}
