package org.hibernate.validation.engine.groups;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
@GroupSequence({ Default.class, AddressWithInvalidGroupSequence.HighLevelCoherence.class })
public class AddressWithInvalidGroupSequence extends Address {

}