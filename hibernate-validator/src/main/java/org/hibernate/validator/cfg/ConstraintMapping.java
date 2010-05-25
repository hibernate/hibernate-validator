// $Id$
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
 * Top level class for programmatically configured constraints.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintMapping {
	private final Map<Class<?>, List<ConstraintDefinition<?>>> constraintConfig;
	private final Map<Class<?>, List<CascadeDefinition>> cascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;

	public ConstraintMapping() {
		this.constraintConfig = new HashMap<Class<?>, List<ConstraintDefinition<?>>>();
		this.cascadeConfig = new HashMap<Class<?>, List<CascadeDefinition>>();
		this.configuredClasses = new HashSet<Class<?>>();
		this.defaultGroupSequences = new HashMap<Class<?>, List<Class<?>>>();
	}

	public ConstraintsForType type(Class<?> beanClass) {
		return new ConstraintsForType( beanClass, this );
	}

	protected void addConstraintConfig(ConstraintDefinition<?> definition) {
		Class<?> beanClass = definition.getBeanType();
		configuredClasses.add( beanClass );
		if ( constraintConfig.containsKey( beanClass ) ) {
			constraintConfig.get( beanClass ).add( definition );
		}
		else {
			List<ConstraintDefinition<?>> definitionList = new ArrayList<ConstraintDefinition<?>>();
			definitionList.add( definition );
			constraintConfig.put( beanClass, definitionList );
		}
	}

	protected void addCascadeConfig(CascadeDefinition cascade) {
		Class<?> beanClass = cascade.getBeanType();
		configuredClasses.add( beanClass );
		if ( cascadeConfig.containsKey( beanClass ) ) {
			cascadeConfig.get( beanClass ).add( cascade );
		}
		else {
			List<CascadeDefinition> cascadeList = new ArrayList<CascadeDefinition>();
			cascadeList.add( cascade );
			cascadeConfig.put( beanClass, cascadeList );
		}
	}

	protected void addDefaultGroupSequence(Class<?> beanClass, List<Class<?>> defaultGroupSequence) {
		defaultGroupSequences.put( beanClass, defaultGroupSequence );
	}

	public Map<Class<?>, List<ConstraintDefinition<?>>> getConstraintConfig() {
		return constraintConfig;
	}

	public Map<Class<?>, List<CascadeDefinition>> getCascadeConfig() {
		return cascadeConfig;
	}

	public Collection<Class<?>> getConfiguredClasses() {
		return configuredClasses;
	}

	public List<Class<?>> getDefaultSequence(Class<?> beanType) {
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
}


