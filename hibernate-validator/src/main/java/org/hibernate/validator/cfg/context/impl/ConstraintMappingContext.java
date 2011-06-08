/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.cfg.context.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * Context which collects constraints, cascades etc. configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.om)
 */
public class ConstraintMappingContext {
	private final Map<Class<?>, List<ConfiguredConstraint<?, BeanConstraintLocation>>> constraintConfig;
	private final Map<Class<?>, List<ConfiguredConstraint<?, MethodConstraintLocation>>> methodConstraintConfig;
	private final Map<Class<?>, List<BeanConstraintLocation>> cascadeConfig;
	private final Map<Class<?>, List<MethodConstraintLocation>> methodCascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;
	private final Map<Class<?>, Class<? extends DefaultGroupSequenceProvider<?>>> defaultGroupSequenceProviders;

	public ConstraintMappingContext() {
		this.constraintConfig = newHashMap();
		this.methodConstraintConfig = newHashMap();
		this.cascadeConfig = newHashMap();
		this.methodCascadeConfig = newHashMap();
		this.configuredClasses = newHashSet();
		this.defaultGroupSequences = newHashMap();
		this.defaultGroupSequenceProviders = newHashMap();
	}

	/**
	 * Returns the constraint mapping context from the given constraint mapping.
	 */
	public static ConstraintMappingContext getFromMapping(ConstraintMapping mapping) {
		return new ConstraintMappingContextAccessor( mapping ).getContext();
	}

	/**
	 * Returns all constraint definitions registered with this mapping.
	 *
	 * @return A map with this mapping's constraint definitions. Each key in
	 *         this map represents a bean type, for which the constraint
	 *         definitions in the associated map value are configured.
	 */
	public final Map<Class<?>, List<ConfiguredConstraint<?, BeanConstraintLocation>>> getConstraintConfig() {
		return constraintConfig;
	}

	public Map<Class<?>, List<ConfiguredConstraint<?, MethodConstraintLocation>>> getMethodConstraintConfig() {
		return methodConstraintConfig;
	}

	public final Map<Class<?>, List<BeanConstraintLocation>> getCascadeConfig() {
		return cascadeConfig;
	}

	public final Map<Class<?>, List<MethodConstraintLocation>> getMethodCascadeConfig() {
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

	public final void addCascadeConfig(BeanConstraintLocation cascade) {
		Class<?> beanClass = cascade.getBeanClass();
		configuredClasses.add( beanClass );
		if ( cascadeConfig.containsKey( beanClass ) ) {
			cascadeConfig.get( beanClass ).add( cascade );
		}
		else {
			List<BeanConstraintLocation> cascadeList = newArrayList();
			cascadeList.add( cascade );
			cascadeConfig.put( beanClass, cascadeList );
		}
	}

	public final void addMethodCascadeConfig(MethodConstraintLocation cascade) {
		Class<?> beanClass = cascade.getBeanClass();
		configuredClasses.add( beanClass );
		if ( methodCascadeConfig.containsKey( beanClass ) ) {
			methodCascadeConfig.get( beanClass ).add( cascade );
		}
		else {
			List<MethodConstraintLocation> cascadeList = newArrayList();
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

	public final void addConstraintConfig(ConfiguredConstraint<?, BeanConstraintLocation> constraint) {
		Class<?> beanClass = constraint.getLocation().getBeanClass();
		configuredClasses.add( beanClass );
		if ( constraintConfig.containsKey( beanClass ) ) {
			constraintConfig.get( beanClass ).add( constraint );
		}
		else {
			List<ConfiguredConstraint<?, BeanConstraintLocation>> definitionList = newArrayList();
			definitionList.add( constraint );
			constraintConfig.put( beanClass, definitionList );
		}
	}

	public final void addMethodConstraintConfig(ConfiguredConstraint<?, MethodConstraintLocation> constraint) {
		Class<?> beanClass = constraint.getLocation().getBeanClass();
		configuredClasses.add( beanClass );
		if ( methodConstraintConfig.containsKey( beanClass ) ) {
			methodConstraintConfig.get( beanClass ).add( constraint );
		}
		else {
			List<ConfiguredConstraint<?, MethodConstraintLocation>> definitionList = newArrayList();
			definitionList.add( constraint );
			methodConstraintConfig.put( beanClass, definitionList );
		}
	}

	/**
	 * Provides access to the members of a {@link ConstraintMapping}.
	 */
	private static class ConstraintMappingContextAccessor extends ConstraintMapping {

		private ConstraintMappingContextAccessor(ConstraintMapping original) {
			super( original );
		}

		private ConstraintMappingContext getContext() {
			return context;
		}
	}
}
