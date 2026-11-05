package org.hibernate.validator.internal.properties;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;

/**
 * @author Guillaume Smet
 */
public interface Field extends Property {

	@Override
	default ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.FIELD;
	}
}
