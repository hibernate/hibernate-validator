//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

//end::include[]
import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

//tag::include[]
public class MyConstraintMappingContributor implements ConstraintMappingContributor {

	@Override
	public void createConstraintMappings(ConstraintMappingBuilder builder) {
		builder.addConstraintMapping()
			.type( Marathon.class )
				.property( "name", METHOD )
					.constraint( new NotNullDef() )
				.property( "numberOfHelpers", FIELD )
					.constraint( new MinDef().value( 1 ) );

		builder.addConstraintMapping()
			.type( Runner.class )
				.property( "paidEntryFee", FIELD )
					.constraint( new AssertTrueDef() );
	}
}
//end::include[]
