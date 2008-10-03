//$Id$
package org.hibernate.validator;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SingleTableSubclass;

/**
 * Check a not null restriction on an object
 * and apply the equivalent constraint on hibernate metadata.
 *
 * @author Gavin King
 */
public class NotNullValidator implements Validator<NotNull>, PropertyConstraint, Serializable {

	public boolean isValid(Object value) {
		return value != null;
	}

	public void initialize(NotNull parameters) {
	}

	@SuppressWarnings("unchecked")
	public void apply(Property property) {
		if ( ! ( property.getPersistentClass() instanceof SingleTableSubclass ) ) {
			//single table should not be forced to null
			if ( !property.isComposite() ) { //composite should not add not-null on all columns
				Iterator<Column> iter = (Iterator<Column>) property.getColumnIterator();
				while ( iter.hasNext() ) {
					iter.next().setNullable( false );
				}
			}
		}
	}
}
