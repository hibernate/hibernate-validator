//$Id$
package org.hibernate.validator;

import org.hibernate.mapping.PersistentClass;

/**
 * Interface implemented by the validator
 * when a constraint may be represented in the
 * hibernate metadata
 *
 * @author Gavin King
 */
public interface PersistentClassConstraint {
	/**
	 * Apply the constraint in the hibernate metadata
	 *
	 * @param persistentClass
	 */
	public void apply(PersistentClass persistentClass);
}
