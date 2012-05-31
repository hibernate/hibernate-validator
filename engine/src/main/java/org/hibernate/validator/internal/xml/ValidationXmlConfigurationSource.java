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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConfigurationSource;

/**
 * Wrapper class for the bootstrap parameters defined in <i>validation.xml</i>
 *
 * @author Hardy Ferentschik
 */
public class ValidationXmlConfigurationSource implements ConfigurationSource {
	private final String defaultProviderClassName;
	private final String constraintValidatorFactoryClassName;
	private final String messageInterpolatorClassName;
	private final String traversableResolverClassName;
	private final String parameterNameProviderClassName;
	private final Set<String> constraintMappingResourcePath;
	private final Map<String, String> properties;

	public ValidationXmlConfigurationSource() {
		this.defaultProviderClassName = null;
		this.constraintValidatorFactoryClassName = null;
		this.messageInterpolatorClassName = null;
		this.traversableResolverClassName = null;
		this.parameterNameProviderClassName = null;
		this.constraintMappingResourcePath = new HashSet<String>();
		this.properties = new HashMap<String, String>();
	}

	public ValidationXmlConfigurationSource(String defaultProviderClassName,
											String constraintValidatorFactoryClassName,
											String messageInterpolatorClassName,
											String traversableResolverClassName,
											String parameterNameProviderClassName,
											Set<String> constraintMappingResourcePath,
											Map<String, String> properties) {
		this.defaultProviderClassName = defaultProviderClassName;
		this.constraintValidatorFactoryClassName = constraintValidatorFactoryClassName;
		this.messageInterpolatorClassName = messageInterpolatorClassName;
		this.traversableResolverClassName = traversableResolverClassName;
		this.parameterNameProviderClassName = parameterNameProviderClassName;
		this.constraintMappingResourcePath = constraintMappingResourcePath;
		this.properties = properties;
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
	public Set<String> getConstraintMappingResourcePath() {
		return constraintMappingResourcePath;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValidationXmlConfigurationSource" );
		sb.append( "{defaultProviderClassName='" ).append( defaultProviderClassName ).append( '\'' );
		sb.append( ", constraintValidatorFactoryClassName='" )
				.append( constraintValidatorFactoryClassName )
				.append( '\'' );
		sb.append( ", messageInterpolatorClassName='" ).append( messageInterpolatorClassName ).append( '\'' );
		sb.append( ", traversableResolverClassName='" ).append( traversableResolverClassName ).append( '\'' );
		sb.append( ", parameterNameProviderClassName='" ).append( parameterNameProviderClassName ).append( '\'' );
		sb.append( ", constraintMappingResourcePath=" ).append( constraintMappingResourcePath );
		sb.append( ", properties=" ).append( properties );
		sb.append( '}' );
		return sb.toString();
	}
}


