/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.GroupSequence;

/**
 * @author Jan-Willem Willebrands
 */
@GroupSequence({ValidationGroup1.class, ValidationGroup2.class})
public interface CompoundGroup {

}
