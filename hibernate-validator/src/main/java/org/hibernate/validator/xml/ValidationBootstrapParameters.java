/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.xml;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.spi.ValidationProvider;

/**
 * @author Hardy Ferentschik
 */
public class ValidationBootstrapParameters {
	private ConstraintValidatorFactory constraintValidatorFactory;
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private ValidationProvider<?> provider;
	private Class<? extends ValidationProvider<?>> providerClass = null;
	private final Map<String, String> configProperties = new HashMap<String, String>();
	private final Set<InputStream> mappings = new HashSet<InputStream>();

	public final ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public final void setConstraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.constraintValidatorFactory = constraintValidatorFactory;
	}

	public final MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public final void setMessageInterpolator(MessageInterpolator messageInterpolator) {
		this.messageInterpolator = messageInterpolator;
	}

	public final ValidationProvider<?> getProvider() {
		return provider;
	}

	public final void setProvider(ValidationProvider<?> provider) {
		this.provider = provider;
	}

	public final Class<? extends ValidationProvider<?>> getProviderClass() {
		return providerClass;
	}

	public final void setProviderClass(Class<? extends ValidationProvider<?>> providerClass) {
		this.providerClass = providerClass;
	}

	public final TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public final void setTraversableResolver(TraversableResolver traversableResolver) {
		this.traversableResolver = traversableResolver;
	}

	public final void addConfigProperty(String key, String value) {
		configProperties.put( key, value );
	}

	public final void addMapping(InputStream in) {
		mappings.add( in );
	}

	public final void addAllMappings(Set<InputStream> mappings) {
		this.mappings.addAll( mappings );
	}

	public final Set<InputStream> getMappings() {
		return Collections.unmodifiableSet( mappings );
	}

	public final Map<String, String> getConfigProperties() {
		return Collections.unmodifiableMap( configProperties );
	}
}
