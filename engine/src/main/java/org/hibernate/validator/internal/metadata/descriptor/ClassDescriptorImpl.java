/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.validation.metadata.ElementDescriptor;

/**
 * Describes a validated type class-level constraints.
 *
 * @author Marko Bekhta
 */
public class ClassDescriptorImpl extends ElementDescriptorImpl implements ElementDescriptor {

	public ClassDescriptorImpl(Type beanType,
			Set<ConstraintDescriptorImpl<?>> constraints,
			boolean defaultGroupSequenceRedefined,
			List<Class<?>> defaultGroupSequence) {
		super( beanType, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( "{beanType=" ).append( getElementClass() );
		sb.append( '}' );
		return sb.toString();
	}
}
