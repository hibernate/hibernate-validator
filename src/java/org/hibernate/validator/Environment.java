//$Id: $
package org.hibernate.validator;

/**
 * Hibernate Validator Event properties
 * The properties are retrieved from Hibernate
 * (hibernate.properties, hibernate.cfg.xml, persistence.xml or Configuration API)
 *
 * @author Emmanuel Bernard
 */
public class Environment {
	/**
	 * Message interpolator class used. The same instance is shared across all ClassValidators
	 */
	public static final String MESSAGE_INTERPOLATOR_CLASS = "hibernate.validator.message_interpolator_class";

	/**
	 * Apply DDL changes on Hibernate metamodel when using validator with Hibernate Annotations. Default to true.
	 */
	public static final String APPLY_TO_DDL = "hibernate.validator.apply_to_ddl";

	/**
	 * Enable listeners auto registration in Hibernate Annotations and EntityManager. Default to true.
	 */
	public static final String AUTOREGISTER_LISTENERS = "hibernate.validator.autoregister_listeners";
}
