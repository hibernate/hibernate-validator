//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

//end::include[]

import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

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
