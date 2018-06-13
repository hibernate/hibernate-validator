/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.jpa;

import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

/**
 * @author Gunnar Morling
 */
public class WandConstraintMappingContributor implements ConstraintMappingContributor {

	@Override
	public void createConstraintMappings(ConstraintMappingBuilder builder) {
		builder.addConstraintMapping()
			.type( Wand.class )
				.field( "brand" )
					.constraint( new SizeDef().min( 5 ) );
	}
}
