/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.nodenameprovider;

import org.hibernate.validator.Incubating;

/// This interface is used to resolve the name of a property node when creating the property path.
///
/// @author Damir Alibegovic
/// @since 6.1.0
@Incubating
public interface PropertyNodeNameProvider {
	/// Returns the resolved name of a property.
	///
	/// Depending on the subtype of the [Property],
	/// a different strategy for name resolution could be applied, defaulting to [Property#getName()]. For example:
	///
	/// ```
	/// if (property instanceof [JavaBeanProperty]){
	///// for instance, generate a property name based on the annotations of the property
	///} else {
	///     return property.getName();
	///}
	///```
	///
	/// @param property whose name needs to be resolved
	/// @return String representing the resolved name
	/// @deprecated Implement [#getName(Property, PropertyNodeNameProviderContext) ] instead.
	@Deprecated(since = "9.2", forRemoval = true)
	default String getName(Property property) {
		throw new UnsupportedOperationException( "This method is not used by Hibernate Validator, implement/use the alternative one that accepts the PropertyNodeNameProviderContext instead." );
	}

	/// Returns the resolved name of a property.
	///
	/// Depending on the subtype of the [Property],
	/// a different strategy for name resolution could be applied, defaulting to [Property#getName()]. For example:
	///
	/// ```
	/// if (property instanceof [JavaBeanProperty]){
	///// for instance, generate a property name based on the annotations of the property
	///} else {
	///     return property.getName();
	///}
	///```
	///
	/// @param property whose name needs to be resolved
	/// @param context  additional context available during the property name resolution
	/// @return String representing the resolved name
	/// @since 9.2
	@Incubating
	default String getName(Property property, PropertyNodeNameProviderContext context) {
		return getName( property );
	}
}
