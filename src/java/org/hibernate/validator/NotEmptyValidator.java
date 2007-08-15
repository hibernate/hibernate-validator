//$Id: $
package org.hibernate.validator;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Iterator;

import org.hibernate.mapping.Property;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.Column;

/**
 * Check the non emptyness of the element
 *
 * @author Gavin King
 */
public class NotEmptyValidator implements Validator<NotEmpty>, PropertyConstraint, Serializable {

	public void initialize(NotEmpty parameters) {
	}

	public boolean isValid(Object value) {
		if ( value == null ) return false;
		if ( value.getClass().isArray() ) {
			return Array.getLength( value ) > 0;
		}
		else if ( value instanceof Collection ) {
			return ( (Collection) value ).size() > 0;
		}
		else if ( value instanceof Map ) {
			return ( (Map) value ).size() > 0;
		}
		else {
			return ( (String) value ).length() > 0;
		}
	}

	@SuppressWarnings("unchecked")
	public void apply(Property property) {
		if ( ! ( property.getPersistentClass() instanceof SingleTableSubclass )
				&& ! ( property.getValue() instanceof Collection ) ) {
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
