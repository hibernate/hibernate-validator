/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryBean implements Bean<ValidatorFactory> {
	private final BeanManager beanManager;
	private final Set<Annotation> qualifiers;
	private final Set<DestructibleBeanInstance<?>> destructibleResources;

	public ValidatorFactoryBean(BeanManager beanManager, Set<Annotation> qualifiers) {
		this.beanManager = beanManager;
		this.destructibleResources = CollectionHelper.newHashSet();
		this.qualifiers = CollectionHelper.newHashSet();
		this.qualifiers.addAll( qualifiers );
		this.qualifiers.add(
				new AnnotationLiteral<Any>() {
				}
		);
	}

	@Override
	public Class<?> getBeanClass() {
		return ValidatorFactoryImpl.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return ApplicationScoped.class;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public Set<Type> getTypes() {
		Set<Type> types = CollectionHelper.newHashSet();

		types.add( ValidatorFactory.class );
		types.add( Object.class );

		return types;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public ValidatorFactory create(CreationalContext<ValidatorFactory> ctx) {
		Configuration<?> config = Validation.byProvider( org.hibernate.validator.HibernateValidator.class )
				.configure();

		config.constraintValidatorFactory( createConstraintValidatorFactory( config ) );
		config.messageInterpolator( createMessageInterpolator( config ) );
		config.traversableResolver( createTraversableResolver( config ) );
		config.parameterNameProvider( createParameterNameProvider( config ) );

		return config.buildValidatorFactory();
	}

	@Override
	public void destroy(ValidatorFactory instance, CreationalContext<ValidatorFactory> ctx) {
		for ( DestructibleBeanInstance<?> resource : destructibleResources ) {
			resource.destroy();
		}
		instance.close();
	}

	private MessageInterpolator createMessageInterpolator(Configuration<?> config) {
		BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();
		String messageInterpolatorFqcn = bootstrapConfiguration.getMessageInterpolatorClassName();

		if ( messageInterpolatorFqcn == null ) {
			return config.getDefaultMessageInterpolator();
		}

		@SuppressWarnings("unchecked")
		Class<MessageInterpolator> messageInterpolatorClass = (Class<MessageInterpolator>) ReflectionHelper.loadClass(
				messageInterpolatorFqcn,
				this.getClass()
		);

		return createInstance( messageInterpolatorClass );
	}

	private TraversableResolver createTraversableResolver(Configuration<?> config) {
		BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();
		String traversableResolverFqcn = bootstrapConfiguration.getTraversableResolverClassName();

		if ( traversableResolverFqcn == null ) {
			return config.getDefaultTraversableResolver();
		}

		@SuppressWarnings("unchecked")
		Class<TraversableResolver> traversableResolverClass = (Class<TraversableResolver>) ReflectionHelper.loadClass(
				traversableResolverFqcn,
				this.getClass()
		);

		return createInstance( traversableResolverClass );
	}

	private ParameterNameProvider createParameterNameProvider(Configuration<?> config) {
		BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();
		String parameterNameProviderFqcn = bootstrapConfiguration.getParameterNameProviderClassName();

		if ( parameterNameProviderFqcn == null ) {
			return config.getDefaultParameterNameProvider();
		}

		@SuppressWarnings("unchecked")
		Class<ParameterNameProvider> parameterNameProviderClass = (Class<ParameterNameProvider>) ReflectionHelper.loadClass(
				parameterNameProviderFqcn,
				this.getClass()
		);

		return createInstance( parameterNameProviderClass );
	}

	private ConstraintValidatorFactory createConstraintValidatorFactory(Configuration<?> config) {
		BootstrapConfiguration configSource = config.getBootstrapConfiguration();
		String constraintValidatorFactoryFqcn = configSource.getConstraintValidatorFactoryClassName();

		if ( constraintValidatorFactoryFqcn == null ) {
			// use default
			return createInstance( InjectingConstraintValidatorFactory.class );
		}

		@SuppressWarnings("unchecked")
		Class<ConstraintValidatorFactory> constraintValidatorFactoryClass = (Class<ConstraintValidatorFactory>) ReflectionHelper
				.loadClass(
						constraintValidatorFactoryFqcn,
						this.getClass()
				);

		return createInstance( constraintValidatorFactoryClass );
	}

	private <T> T createInstance(Class<T> type) {
		DestructibleBeanInstance<T> destructibleInstance = new DestructibleBeanInstance<T>( beanManager, type );
		destructibleResources.add( destructibleInstance );
		return destructibleInstance.getInstance();
	}
}
