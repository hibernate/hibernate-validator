/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.util.Map;

import org.hibernate.accessor.spi.AccessContext;
import org.hibernate.accessor.spi.AccessorConfiguration;

public class HibernateValidatorAccessContext implements AccessContext {

	public static final String DEFAULT_ACCESSOR_FACTORY_NAME = "default";

	public static AccessorConfiguration configuration() {
		return new AccessorConfiguration( new HibernateValidatorAccessContext(), Map.of() );
	}

	private final MethodHandles.Lookup lookup;

	private HibernateValidatorAccessContext() {
		this.lookup = MethodHandles.lookup();
	}

	@Override
	public MethodHandles.Lookup lookup() {
		return lookup;
	}

	@Override
	public void ensureReads(Module target) {
		lookup.lookupClass().getModule().addReads( target );
	}

	@Override
	public void makeAccessible(AccessibleObject member) {
		member.setAccessible( true );
	}
}
