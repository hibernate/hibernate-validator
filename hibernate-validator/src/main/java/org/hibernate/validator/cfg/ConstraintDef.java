/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Payload;

/**
 * Base class for all constraint definition types. Each sub type represents a
 * single constraint annotation type and allows to add this constraint to a bean
 * class in a programmatic type-safe way with help of the
 * {@link ConstraintMapping} API.
 *
 * @param <C> The type of a concrete sub type. Following to the
 * "self referencing generic type" pattern each sub type has to be
 * parametrized with itself.
 * @param <A> The constraint annotation type represented by a concrete sub type.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public abstract class ConstraintDef<C extends ConstraintDef<C, A>, A extends Annotation> {

	// Note on visibility of members: These members are intentionally made
	// protected and published by a sub-class for internal use. There aren't
	// public getters as they would pollute the fluent definition API.

	/**
	 * The constraint annotation type of this definition.
	 */
	protected final Class<A> constraintType;

	/**
	 * A map with the annotation parameters of this definition. Keys are
	 * property names of this definition's annotation type, values are
	 * annotation parameter values of the appropriate types.
	 */
	protected final Map<String, Object> parameters;

	protected ConstraintDef(Class<A> constraintType) {
		this.constraintType = constraintType;
		this.parameters = new HashMap<String, Object>();
	}

	protected ConstraintDef(ConstraintDef<?, A> original) {
		this.constraintType = original.constraintType;
		this.parameters = original.parameters;
	}

	@SuppressWarnings("unchecked")
	private C getThis() {
		return (C) this;
	}

	protected C addParameter(String key, Object value) {
		parameters.put( key, value );
		return getThis();
	}

	public C message(String message) {
		addParameter( "message", message );
		return getThis();
	}

	public C groups(Class<?>... groups) {
		addParameter( "groups", groups );
		return getThis();
	}

	public C payload(Class<? extends Payload>... payload) {
		addParameter( "payload", payload );
		return getThis();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( this.getClass().getName() );
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( ", parameters=" ).append( parameters );
		sb.append( '}' );
		return sb.toString();
	}
}
