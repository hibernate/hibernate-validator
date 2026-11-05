package org.hibernate.validator.internal.properties;

import org.hibernate.accessor.ValueReader;

/**
 * @author Marko Bekhta
 */
public interface Property extends Constrainable {

	String getPropertyName();

	String getResolvedPropertyName();

	ValueReader<?> createAccessor();
}
