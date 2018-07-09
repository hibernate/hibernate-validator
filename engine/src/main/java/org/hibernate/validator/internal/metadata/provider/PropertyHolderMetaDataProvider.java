/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.util.Optional;

import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;

/**
 * @author Marko Bekhta
 */
public interface PropertyHolderMetaDataProvider {

	Optional<PropertyHolderConfiguration> getBeanConfiguration(String mappingName);
}
