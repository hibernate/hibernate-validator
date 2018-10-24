/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

/**
 * @author Marko Bekhta
 */
public interface CascadableContainerElementConstraintMappingContext extends ContainerElementConstraintMappingContext,
		Cascadable<CascadableContainerElementConstraintMappingContext> {
}
