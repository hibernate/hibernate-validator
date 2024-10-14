/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.GroupSequence;

/**
 * @author Jan-Willem Willebrands
 */
@GroupSequence({ ValidationGroup1.class, ValidationGroup2.class })
public interface CompoundGroup {

}
