/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.tracking;

import java.util.function.BooleanSupplier;

import org.hibernate.validator.Incubating;

/**
 * Helps to determine whether the processed beans of a particular type have to be tracked
 * when cascaded into. Used during metadata building step.
 * <p>
 * The default voter returns {@link Vote#DEFAULT} which means that if the bean has any cascading properties,
 * it will be considered as such that requires tracking,
 * and if there are no cascading properties -- the bean tracking is ignored for this type.
 */
@Incubating
public interface ProcessedBeansTrackingVoter {

	Vote isEnabledForBean(Class<?> beanClass, boolean hasCascadables);

	Vote isEnabledForReturnValue(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables);

	Vote isEnabledForParameters(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables);

	enum Vote {

		DEFAULT, NON_TRACKING, TRACKING;

		public static boolean voteToTracking(Vote vote, BooleanSupplier processedByDefault) {
			return switch ( vote ) {
				case NON_TRACKING -> false;
				case TRACKING -> true;
				default -> processedByDefault.getAsBoolean();
			};
		}
	}
}
