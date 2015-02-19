/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.cfg;

import org.hibernate.validator.cfg.ConstraintMapping;

/**
 * Implementations contribute {@link ConstraintMapping}s to validator factory.
 * <p>
 * A constraint mapping contributor can be configured in {@code META-INF/validation.xml}, using the property
 * {@link org.hibernate.validator.HibernateValidatorConfiguration#CONSTRAINT_MAPPING_CONTRIBUTOR}, thus allowing to set
 * up constraints to be validated by default validators dynamically via the API for programmatic constraint declaration.
 * <p>
 * Implementations must have a no-args constructor.
 * <p>
 * One or more mappings can be added as shown in the following:
 *
 * <pre>
 * {@code
 * public static class MyConstraintMappingContributor implements ConstraintMappingContributor {
 *
 *     public void createConstraintMappings(ConstraintMappingBuilder builder) {
 *         builder.addConstraintMapping()
 *             .type( Marathon.class )
 *                 .property( "name", METHOD )
 *                     .constraint( new NotNullDef() )
 *                 .property( "numberOfHelpers", FIELD )
 *                     .constraint( new MinDef().value( 1 ) );
 *
 *         builder.addConstraintMapping()
 *             .type( Runner.class )
 *                 .property( "paidEntryFee", FIELD )
 *                     .constraint( new AssertTrueDef() );
 *     }
 * }
 * }
 * </pre>
 * <p>
 * @see org.hibernate.validator.HibernateValidatorConfiguration#CONSTRAINT_MAPPING_CONTRIBUTOR
 * @author Gunnar Morling
 */
public interface ConstraintMappingContributor {

	/**
	 * Callback invoked during validator factory creation. Any constraint mapping configured via the passed builder
	 * will be added to the mappings of the factory.
	 *
	 * @param builder A builder for adding one or more constraint mappings
	 */
	void createConstraintMappings(ConstraintMappingBuilder builder);

	/**
	 * A builder for adding constraint mappings.
	 */
	interface ConstraintMappingBuilder {

		/**
		 * Adds a new constraint mapping.
		 *
		 * @return The constraint mapping for further configuration via the fluent API.
		 */
		ConstraintMapping addConstraintMapping();
	}
}
