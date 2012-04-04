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
package org.hibernate.validator.internal.cfg.context;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Context which collects constraints, cascades etc. configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
@SuppressWarnings("deprecation")
public class ConstraintMappingContext {
	private final Map<Class<?>, Set<ConfiguredConstraint<?, BeanConstraintLocation>>> constraintConfig;
	private final Map<Class<?>, Set<ConfiguredConstraint<?, MethodConstraintLocation>>> methodConstraintConfig;
	private final Map<Class<?>, Set<BeanConstraintLocation>> cascadeConfig;
	private final Map<Class<?>, Set<MethodConstraintLocation>> methodCascadeConfig;
	private final Set<Class<?>> configuredClasses;
	private final Map<Class<?>, List<Class<?>>> defaultGroupSequences;
	private final Map<Class<?>, Class<? extends DefaultGroupSequenceProvider<?>>> deprecatedDefaultGroupSequenceProviders;
	private final Map<Class<?>, Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<?>>> defaultGroupSequenceProviders;
	private final AnnotationProcessingOptions annotationProcessingOptions;

	public ConstraintMappingContext() {
		this.constraintConfig = newHashMap();
		this.methodConstraintConfig = newHashMap();
		this.cascadeConfig = newHashMap();
		this.methodCascadeConfig = newHashMap();
		this.configuredClasses = newHashSet();
		this.defaultGroupSequences = newHashMap();
		this.deprecatedDefaultGroupSequenceProviders = newHashMap();
		this.defaultGroupSequenceProviders = newHashMap();
		this.annotationProcessingOptions = new AnnotationProcessingOptions();
	}

	/**
	 * Returns the constraint mapping context from the given constraint mapping.
	 *
	 * @param mapping the programmatic constraint mapping
	 *
	 * @return returns the constraint mapping context from the given constraint mapping
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
	public final Map<Class<?>, Set<ConfiguredConstraint<?, BeanConstraintLocation>>> getConstraintConfig() {
		return constraintConfig;
	}

	public Map<Class<?>, Set<ConfiguredConstraint<?, MethodConstraintLocation>>> getMethodConstraintConfig() {
		return methodConstraintConfig;
	}

	public final Map<Class<?>, Set<BeanConstraintLocation>> getCascadeConfig() {
		return cascadeConfig;
	}

	public final Map<Class<?>, Set<MethodConstraintLocation>> getMethodCascadeConfig() {
		return methodCascadeConfig;
	}

	public final Collection<Class<?>> getConfiguredClasses() {
		return configuredClasses;
	}

	public final List<Class<?>> getDefaultSequence(Class<?> beanType) {
		return defaultGroupSequences.get( beanType );
	}

	public final AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	/**
	 * Returns the class of the default group sequence provider defined
	 * for the given bean type.
	 *
	 * @param beanType The bean type.
	 *
	 * @return The default group sequence provider defined class or {@code null} if none.
	 */
	public final <T> Class<? extends DefaultGroupSequenceProvider<? super T>> getDeprecatedDefaultGroupSequenceProvider(Class<T> beanType) {
		@SuppressWarnings("unchecked")
		Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass = (Class<? extends DefaultGroupSequenceProvider<? super T>>) deprecatedDefaultGroupSequenceProviders
				.get( beanType );
		return providerClass;
	}

	/**
	 * Returns the class of the default group sequence provider defined
	 * for the given bean type.
	 *
	 * @param beanType The bean type.
	 *
	 * @return The default group sequence provider defined class or {@code null} if none.
	 */
	public final <T> Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<? super T>> getDefaultGroupSequenceProvider(Class<T> beanType) {
		@SuppressWarnings("unchecked")
		Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<? super T>> providerClass = (Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<? super T>>) defaultGroupSequenceProviders
				.get( beanType );
		return providerClass;
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
			Set<BeanConstraintLocation> cascadeList = newHashSet();
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
			Set<MethodConstraintLocation> cascadeList = newHashSet();
			cascadeList.add( cascade );
			methodCascadeConfig.put( beanClass, cascadeList );
		}
	}

	public final void addDefaultGroupSequence(Class<?> beanClass, List<Class<?>> defaultGroupSequence) {
		configuredClasses.add( beanClass );
		defaultGroupSequences.put( beanClass, defaultGroupSequence );
	}

	public final <T> void addDeprecatedDefaultGroupSequenceProvider(Class<T> beanClass, Class<? extends DefaultGroupSequenceProvider<? super T>> defaultGroupSequenceProviderClass) {
		configuredClasses.add( beanClass );
		deprecatedDefaultGroupSequenceProviders.put( beanClass, defaultGroupSequenceProviderClass );
	}

	public final <T> void addDefaultGroupSequenceProvider(Class<T> beanClass, Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<? super T>> defaultGroupSequenceProviderClass) {
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
			Set<ConfiguredConstraint<?, BeanConstraintLocation>> definitionList = newHashSet();
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
			Set<ConfiguredConstraint<?, MethodConstraintLocation>> definitionList = newHashSet();
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
