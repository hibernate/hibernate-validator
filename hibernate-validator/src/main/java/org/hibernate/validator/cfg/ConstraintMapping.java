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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Top level class for constraints configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ConstraintMapping {
	private final Map<Class<?>, List<ConstraintDef<?, ?>>> constraintConfig;
	private final Map<Class<?>, List<CascadeDef>> cascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;

	public ConstraintMapping() {
		this.constraintConfig = new HashMap<Class<?>, List<ConstraintDef<?, ?>>>();
		this.cascadeConfig = new HashMap<Class<?>, List<CascadeDef>>();
		this.configuredClasses = new HashSet<Class<?>>();
		this.defaultGroupSequences = new HashMap<Class<?>, List<Class<?>>>();
	}

	/**
	 * Starts defining constraints on the specified bean class.
	 *
	 * @param beanClass The bean class on which to define constraints. All constraints defined after calling this method
	 * are added to the bean of the type {@code beanClass} until the next call of {@code type}.
	 *
	 * @return Instance allowing for defining constraints on the specified class.
	 */
	public final ConstraintsForType type(Class<?> beanClass) {
		return new ConstraintsForType( beanClass, this );
	}

	/**
	 * Returns all constraint definitions registered with this mapping.
	 *
	 * @return A map with this mapping's constraint definitions. Each key in
	 *         this map represents a bean type, for which the constraint
	 *         definitions in the associated map value are configured.
	 */
	public final Map<Class<?>, List<ConstraintDefAccessor<?>>> getConstraintConfig() {

		Map<Class<?>, List<ConstraintDefAccessor<?>>> newDefinitions = new HashMap<Class<?>, List<ConstraintDefAccessor<?>>>();

		for ( Map.Entry<Class<?>, List<ConstraintDef<?, ?>>> entry : constraintConfig.entrySet() ) {

			List<ConstraintDefAccessor<?>> newList = new ArrayList<ConstraintDefAccessor<?>>();

			for ( ConstraintDef<?, ?> definition : entry.getValue() ) {
				newList.add( ConstraintDefAccessor.getInstance( definition ) );
			}

			newDefinitions.put( entry.getKey(), newList );
		}

		return newDefinitions;
	}

	public final Map<Class<?>, List<CascadeDef>> getCascadeConfig() {
		return cascadeConfig;
	}

	public final Collection<Class<?>> getConfiguredClasses() {
		return configuredClasses;
	}

	public final List<Class<?>> getDefaultSequence(Class<?> beanType) {
		if ( defaultGroupSequences.containsKey( beanType ) ) {
			return defaultGroupSequences.get( beanType );
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintMapping" );
		sb.append( "{cascadeConfig=" ).append( cascadeConfig );
		sb.append( ", constraintConfig=" ).append( constraintConfig );
		sb.append( ", configuredClasses=" ).append( configuredClasses );
		sb.append( ", defaultGroupSequences=" ).append( defaultGroupSequences );
		sb.append( '}' );
		return sb.toString();
	}

	protected final void addCascadeConfig(CascadeDef cascade) {
		Class<?> beanClass = cascade.getBeanType();
		configuredClasses.add( beanClass );
		if ( cascadeConfig.containsKey( beanClass ) ) {
			cascadeConfig.get( beanClass ).add( cascade );
		}
		else {
			List<CascadeDef> cascadeList = new ArrayList<CascadeDef>();
			cascadeList.add( cascade );
			cascadeConfig.put( beanClass, cascadeList );
		}
	}

	protected final void addDefaultGroupSequence(Class<?> beanClass, List<Class<?>> defaultGroupSequence) {
		defaultGroupSequences.put( beanClass, defaultGroupSequence );
	}

	protected final void addConstraintConfig(ConstraintDef<?, ?> definition) {
		Class<?> beanClass = definition.beanType;
		configuredClasses.add( beanClass );
		if ( constraintConfig.containsKey( beanClass ) ) {
			constraintConfig.get( beanClass ).add( definition );
		}
		else {
			List<ConstraintDef<?, ?>> definitionList = new ArrayList<ConstraintDef<?, ?>>();
			definitionList.add( definition );
			constraintConfig.put( beanClass, definitionList );
		}
	}
}


