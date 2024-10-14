/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.resolver;

import jakarta.validation.Path;

abstract class AbstractTraversableHolder {

	private final Object traversableObject;
	private final Path.Node traversableProperty;
	private final int hashCode;

	protected AbstractTraversableHolder(Object traversableObject, Path.Node traversableProperty) {
		this.traversableObject = traversableObject;
		this.traversableProperty = traversableProperty;
		this.hashCode = buildHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || !( o instanceof AbstractTraversableHolder ) ) {
			return false;
		}

		AbstractTraversableHolder that = (AbstractTraversableHolder) o;

		if ( traversableObject != null ? ( traversableObject != that.traversableObject ) : that.traversableObject != null ) {
			return false;
		}
		if ( !traversableProperty.equals( that.traversableProperty ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public int buildHashCode() {
		// HV-1013 Using identity hash code in order to avoid calling hashCode() of objects which may
		// be handling null properties not correctly
		int result = traversableObject != null ? System.identityHashCode( traversableObject ) : 0;
		result = 31 * result + traversableProperty.hashCode();
		return result;
	}
}
