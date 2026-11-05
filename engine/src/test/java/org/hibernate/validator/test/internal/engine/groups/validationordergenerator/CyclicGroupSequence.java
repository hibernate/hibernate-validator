package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import jakarta.validation.GroupSequence;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence(value = CyclicGroupSequence.class)
public interface CyclicGroupSequence {
}
