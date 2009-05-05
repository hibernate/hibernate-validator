package org.hibernate.validation.engine.graphnavigation;

import javax.validation.GroupSequence;

/**
 * @author Emmanuel Bernard
 */
@GroupSequence( {ChildFirst.class, ParentSecond.class } )
public interface ProperOrder {
}
