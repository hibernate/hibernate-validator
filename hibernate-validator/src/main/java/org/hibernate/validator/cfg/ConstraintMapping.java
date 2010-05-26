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

import java.lang.annotation.Annotation;
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
 */
public class ConstraintMapping {
	private final Map<Class<?>, List<ConstraintDefAccessor<?>>> constraintConfig;
	private final Map<Class<?>, List<CascadeDef>> cascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;

	public ConstraintMapping() {
		this.constraintConfig = new HashMap<Class<?>, List<ConstraintDefAccessor<?>>>();
		this.cascadeConfig = new HashMap<Class<?>, List<CascadeDef>>();
		this.configuredClasses = new HashSet<Class<?>>();
		this.defaultGroupSequences = new HashMap<Class<?>, List<Class<?>>>();
	}

	public ConstraintsForType type(Class<?> beanClass) {
		return new ConstraintsForType( beanClass, this );
	}

	protected <A extends Annotation> void addConstraintConfig(ConstraintDef<?> definition) {
		Class<?> beanClass = definition.beanType;
		@SuppressWarnings( "unchecked")
		ConstraintDefAccessor<A> defAccessor = new ConstraintDefAccessor<A>(
				beanClass,
				( Class<A> ) definition.constraintType,
				definition.property,
				definition.elementType,
				definition.parameters,
				this
		);

		configuredClasses.add( beanClass );
		if ( constraintConfig.containsKey( beanClass ) ) {
			constraintConfig.get( beanClass ).add( defAccessor );
		}
		else {
			List<ConstraintDefAccessor<?>> definitionList = new ArrayList<ConstraintDefAccessor<?>>();
			definitionList.add( defAccessor );
			constraintConfig.put( beanClass, definitionList );
		}
	}

	protected void addCascadeConfig(CascadeDef cascade) {
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

	protected void addDefaultGroupSequence(Class<?> beanClass, List<Class<?>> defaultGroupSequence) {
		defaultGroupSequences.put( beanClass, defaultGroupSequence );
	}

	public Map<Class<?>, List<ConstraintDefAccessor<?>>> getConstraintConfig() {
		return constraintConfig;
	}

	public Map<Class<?>, List<CascadeDef>> getCascadeConfig() {
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


