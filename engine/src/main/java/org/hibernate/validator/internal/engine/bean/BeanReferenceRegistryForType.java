/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A registry for beans of a given exposed type that were explicitly registered through a
 * {@link org.hibernate.validator.spi.bean.BeanConfigurer}.
 *
 * @param <T> The type exposed by beans in this registry.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/impl/BeanReferenceRegistryForType.java">
 *      Original concept from Hibernate Search</a>
 */
public class BeanReferenceRegistryForType<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<T> exposedType;
	private final List<BeanReference<T>> all = new ArrayList<>();
	private final Map<String, BeanReference<T>> named = new TreeMap<>();

	public BeanReferenceRegistryForType(Class<T> exposedType) {
		this.exposedType = exposedType;
	}

	public final List<BeanReference<T>> all() {
		return Collections.unmodifiableList( all );
	}

	public Map<String, BeanReference<T>> named() {
		return Collections.unmodifiableMap( named );
	}

	public BeanReference<T> single() {
		if ( all.size() == 1 ) {
			return all.get( 0 );
		}
		else if ( all.size() > 1 ) {
			throw LOG.getMultipleConfiguredBeanReferencesException(
					exposedType.getName(), all.toString() );
		}
		else {
			return null;
		}
	}

	public BeanReference<T> named(String name) {
		return named.get( name );
	}

	@SuppressWarnings("unchecked")
	void add(BeanReference<? extends T> reference) {
		all.add( (BeanReference<T>) reference );
	}

	@SuppressWarnings("unchecked")
	void add(String name, BeanReference<? extends T> reference) {
		Object previous = named.putIfAbsent( name, (BeanReference<T>) reference );
		if ( previous != null ) {
			throw LOG.getDuplicateBeanReferencesForNameException(
					name, previous.toString(), reference.toString() );
		}
		all.add( (BeanReference<T>) reference );
	}
}
