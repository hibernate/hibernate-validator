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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.context.TypeConstraintMappingCreationalContext;
import org.hibernate.validator.cfg.context.impl.TypeConstraintMappingCreationalContextImpl;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.util.Contracts;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * Top level class for constraints configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.om)
 */
public class ConstraintMapping {
	private final Map<Class<?>, List<ConfiguredConstraint<?>>> constraintConfig;
	private final Map<Class<?>, List<CascadeDef>> cascadeConfig;
	private final Map<Class<?>, List<MethodCascadeDef>> methodCascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;
	private final Map<Class<?>, Class<? extends DefaultGroupSequenceProvider<?>>> defaultGroupSequenceProviders;

	public ConstraintMapping() {
		this.constraintConfig = newHashMap();
		this.cascadeConfig = newHashMap();
		this.methodCascadeConfig = newHashMap();
		this.configuredClasses = newHashSet();
		this.defaultGroupSequences = newHashMap();
		this.defaultGroupSequenceProviders = newHashMap();
	}

	/**
	 * Starts defining constraints on the specified bean class.
	 *
	 * @param beanClass The bean class on which to define constraints. All constraints defined after calling this method
	 * are added to the bean of the type {@code beanClass} until the next call of {@code type}.
	 *
	 * @return Instance allowing for defining constraints on the specified class.
	 */
	public final TypeConstraintMappingCreationalContext type(Class<?> beanClass) {
		
		Contracts.assertNotNull(beanClass, "The bean type must not be null when creating a constraint mapping.");
		
		return new TypeConstraintMappingCreationalContextImpl( beanClass, this );
	}

	/**
	 * Returns all constraint definitions registered with this mapping.
	 *
	 * @return A map with this mapping's constraint definitions. Each key in
	 *         this map represents a bean type, for which the constraint
	 *         definitions in the associated map value are configured.
	 */
	public final Map<Class<?>, List<ConfiguredConstraint<?>>> getConstraintConfig() {
		return constraintConfig;
	}

	public final Map<Class<?>, List<CascadeDef>> getCascadeConfig() {
		return cascadeConfig;
	}

	public final Map<Class<?>, List<MethodCascadeDef>> getMethodCascadeConfig() {
		return methodCascadeConfig;
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

	/**
	 * Returns the class of the default group sequence provider defined
	 * for the given bean type.
	 *
	 * @param beanType The bean type.
	 *
	 * @return The default group sequence provider defined class or {@code null} if none.
	 */
	public final Class<? extends DefaultGroupSequenceProvider<?>> getDefaultGroupSequenceProvider(Class<?> beanType) {
		return defaultGroupSequenceProviders.get( beanType );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintMapping" );
		sb.append( "{cascadeConfig=" ).append( cascadeConfig );
		sb.append( ", methodCascadeConfig=" ).append( methodCascadeConfig );
		sb.append( ", constraintConfig=" ).append( constraintConfig );
		sb.append( ", configuredClasses=" ).append( configuredClasses );
		sb.append( ", defaultGroupSequences=" ).append( defaultGroupSequences );
		sb.append( '}' );
		return sb.toString();
	}

	public final void addCascadeConfig(CascadeDef cascade) {
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

	public final void addMethodCascadeConfig(MethodCascadeDef cascade) {
		Class<?> beanClass = cascade.getBeanType();
		configuredClasses.add( beanClass );
		if ( methodCascadeConfig.containsKey( beanClass ) ) {
			methodCascadeConfig.get( beanClass ).add( cascade );
		}
		else {
			List<MethodCascadeDef> cascadeList = new ArrayList<MethodCascadeDef>();
			cascadeList.add( cascade );
			methodCascadeConfig.put( beanClass, cascadeList );
		}
	}

	public final void addDefaultGroupSequence(Class<?> beanClass, List<Class<?>> defaultGroupSequence) {
		configuredClasses.add( beanClass );
		defaultGroupSequences.put( beanClass, defaultGroupSequence );
	}

	public final <T extends DefaultGroupSequenceProvider<?>> void addDefaultGroupSequenceProvider(Class<?> beanClass, Class<T> defaultGroupSequenceProviderClass) {
		configuredClasses.add( beanClass );
		defaultGroupSequenceProviders.put( beanClass, defaultGroupSequenceProviderClass );
	}

	public final void addConstraintConfig(ConfiguredConstraint<?> constraint) {
		Class<?> beanClass = constraint.getBeanType();
		configuredClasses.add( beanClass );
		if ( constraintConfig.containsKey( beanClass ) ) {
			constraintConfig.get( beanClass ).add( constraint );
		}
		else {
			List<ConfiguredConstraint<?>> definitionList = newArrayList();
			definitionList.add( constraint );
			constraintConfig.put( beanClass, definitionList );
		}
	}
}
