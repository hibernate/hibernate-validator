/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *       hibernate-validator/src/main/docbook/en-US/modules/integration.xml~
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
package org.hibernate.validator.internal.xml;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.validation.BootstrapConfiguration;
import javax.validation.executable.ExecutableType;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Wrapper class for the bootstrap parameters defined in <i>validation.xml</i>
 *
 * @author Hardy Ferentschik
 */
public class BootstrapConfigurationImpl implements BootstrapConfiguration {

	private static final Set<ExecutableType> DEFAULT_VALIDATED_EXECUTABLE_TYPES =
			Collections.unmodifiableSet(
					EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS )
			);

	private static final BootstrapConfiguration DEFAULT_BOOTSTRAP_CONFIGURATION = new BootstrapConfigurationImpl();

	private final String defaultProviderClassName;
	private final String constraintValidatorFactoryClassName;
	private final String messageInterpolatorClassName;
	private final String traversableResolverClassName;
	private final String parameterNameProviderClassName;
	private final Set<String> constraintMappingResourcePaths;
	private final Map<String, String> properties;
	private final Set<ExecutableType> validatedExecutableTypes;

	private BootstrapConfigurationImpl() {
		this.defaultProviderClassName = null;
		this.constraintValidatorFactoryClassName = null;
		this.messageInterpolatorClassName = null;
		this.traversableResolverClassName = null;
		this.parameterNameProviderClassName = null;
		this.validatedExecutableTypes = DEFAULT_VALIDATED_EXECUTABLE_TYPES;
		this.constraintMappingResourcePaths = newHashSet();
		this.properties = newHashMap();
	}

	public BootstrapConfigurationImpl(String defaultProviderClassName,
									  String constraintValidatorFactoryClassName,
									  String messageInterpolatorClassName,
									  String traversableResolverClassName,
									  String parameterNameProviderClassName,
									  EnumSet<ExecutableType> validatedExecutableTypes,
									  Set<String> constraintMappingResourcePaths,
									  Map<String, String> properties) {
		this.defaultProviderClassName = defaultProviderClassName;
		this.constraintValidatorFactoryClassName = constraintValidatorFactoryClassName;
		this.messageInterpolatorClassName = messageInterpolatorClassName;
		this.traversableResolverClassName = traversableResolverClassName;
		this.parameterNameProviderClassName = parameterNameProviderClassName;
		this.validatedExecutableTypes = prepareValidatedExecutableTypes( validatedExecutableTypes );
		this.constraintMappingResourcePaths = constraintMappingResourcePaths;
		this.properties = properties;
	}

	public static BootstrapConfiguration getDefaultBootstrapConfiguration() {
		return DEFAULT_BOOTSTRAP_CONFIGURATION;
	}

	private Set<ExecutableType> prepareValidatedExecutableTypes(EnumSet<ExecutableType> validatedExecutableTypes) {
		if ( validatedExecutableTypes == null ) {
			return DEFAULT_VALIDATED_EXECUTABLE_TYPES;
		}
		if ( validatedExecutableTypes.contains( ExecutableType.ALL ) ) {
			return EnumSet.complementOf( EnumSet.of( ExecutableType.ALL, ExecutableType.NONE ) );
		}
		else if ( validatedExecutableTypes.contains( ExecutableType.NONE ) && validatedExecutableTypes.size() > 1 ) {
			EnumSet<ExecutableType> enumSet = EnumSet.copyOf( validatedExecutableTypes );
			enumSet.remove( ExecutableType.NONE );
			return enumSet;
		}
		else {
			return validatedExecutableTypes;
		}
	}

	@Override
	public String getDefaultProviderClassName() {
		return defaultProviderClassName;
	}

	@Override
	public String getConstraintValidatorFactoryClassName() {
		return constraintValidatorFactoryClassName;
	}

	@Override
	public String getMessageInterpolatorClassName() {
		return messageInterpolatorClassName;
	}

	@Override
	public String getTraversableResolverClassName() {
		return traversableResolverClassName;
	}

	@Override
	public String getParameterNameProviderClassName() {
		return parameterNameProviderClassName;
	}

	@Override
	public Set<String> getConstraintMappingResourcePaths() {
		return newHashSet( constraintMappingResourcePaths );
	}

	@Override
	public Set<ExecutableType> getValidatedExecutableTypes() {
		return newHashSet( validatedExecutableTypes );
	}

	@Override
	public Map<String, String> getProperties() {
		return newHashMap( properties );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BootstrapConfigurationImpl" );
		sb.append( "{defaultProviderClassName='" ).append( defaultProviderClassName ).append( '\'' );
		sb.append( ", constraintValidatorFactoryClassName='" )
				.append( constraintValidatorFactoryClassName )
				.append( '\'' );
		sb.append( ", messageInterpolatorClassName='" ).append( messageInterpolatorClassName ).append( '\'' );
		sb.append( ", traversableResolverClassName='" ).append( traversableResolverClassName ).append( '\'' );
		sb.append( ", parameterNameProviderClassName='" ).append( parameterNameProviderClassName ).append( '\'' );
		sb.append( ", validatedExecutableTypes='" ).append( validatedExecutableTypes ).append( '\'' );
		sb.append( ", constraintMappingResourcePaths=" ).append( constraintMappingResourcePaths ).append( '\'' );
		sb.append( ", properties=" ).append( properties );
		sb.append( '}' );
		return sb.toString();
	}
}
