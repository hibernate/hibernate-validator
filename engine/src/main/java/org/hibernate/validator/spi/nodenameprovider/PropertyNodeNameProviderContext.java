/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.nodenameprovider;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

/// Provides additional context for the [PropertyNodeNameProvider#getName(Property, PropertyNodeNameProviderContext)].
@Incubating
public interface PropertyNodeNameProviderContext {

	/// @return the [GetterPropertySelectionStrategy] configured for the current validator
	GetterPropertySelectionStrategy getGetterPropertySelectionStrategy();
}
