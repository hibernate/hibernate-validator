package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import jakarta.validation.GroupSequence;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence(value = CyclicGroupSequence1.class)
public interface CyclicGroupSequence2 {
}
