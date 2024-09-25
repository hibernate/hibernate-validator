/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

//end::include[]
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

// @formatter:off
//tag::include[]
public class MyConstraintMappingContributor implements ConstraintMappingContributor {

	@Override
	public void createConstraintMappings(ConstraintMappingBuilder builder) {
		builder.addConstraintMapping()
			.type( Marathon.class )
				.getter( "name" )
					.constraint( new NotNullDef() )
				.field( "numberOfHelpers" )
					.constraint( new MinDef().value( 1 ) );

		builder.addConstraintMapping()
			.type( Runner.class )
				.field( "paidEntryFee" )
					.constraint( new AssertTrueDef() );
	}
}
//end::include[]
// @formatter:on
