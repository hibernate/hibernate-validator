/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import jakarta.validation.GroupSequence;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence(value = CyclicGroupSequence1.class)
public interface CyclicGroupSequence2 {
}
