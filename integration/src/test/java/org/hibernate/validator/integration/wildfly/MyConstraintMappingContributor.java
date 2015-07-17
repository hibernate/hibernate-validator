/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import java.lang.annotation.ElementType;

import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

/**
 * @author Gunnar Morling
 */
public class MyConstraintMappingContributor implements ConstraintMappingContributor {

	@Override
	public void createConstraintMappings(ConstraintMappingBuilder builder) {
		builder.addConstraintMapping()
			.type( Broomstick.class )
				.property( "brand", ElementType.FIELD )
					.constraint( new NotNullDef() );
	}
}
