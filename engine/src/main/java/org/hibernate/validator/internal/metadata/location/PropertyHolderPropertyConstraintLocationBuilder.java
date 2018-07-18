/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import org.hibernate.validator.internal.metadata.location.ConstraintLocation.Builder;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Field;
import org.hibernate.validator.internal.util.Contracts;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderPropertyConstraintLocationBuilder implements Builder {

	@Override
	public ConstraintLocation build(Constrainable constrainable) {
		Contracts.assertTrue( constrainable instanceof Field, "Only Field instances are accepted." );

		return ConstraintLocation.forField( constrainable.as( Field.class ) );
	}
}
