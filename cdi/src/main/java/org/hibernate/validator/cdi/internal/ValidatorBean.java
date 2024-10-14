/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * A {@link Bean} representing a {@link Validator}. There is one instance of this type representing the default
 * validator and optionally another instance representing the HV validator in case the default provider is not HV.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ValidatorBean implements Bean<Validator>, PassivationCapable {

	private final BeanManager beanManager;
	private final ValidationProviderHelper validationProviderHelper;
	private final Set<Type> types;
	private final Bean<?> validatorFactoryBean;

	public ValidatorBean(BeanManager beanManager, Bean<?> validatorFactoryBean, ValidationProviderHelper validationProviderHelper) {
		this.beanManager = beanManager;
		this.validatorFactoryBean = validatorFactoryBean;
		this.validationProviderHelper = validationProviderHelper;
		this.types = validationProviderHelper.determineValidatorCdiTypes();
	}

	@Override
	public Class<?> getBeanClass() {
		return validationProviderHelper.getValidatorBeanClass();
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
		return validationProviderHelper.getQualifiers();
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
		return types;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	// TODO to be removed once using CDI API 4.x
	public boolean isNullable() {
		return false;
	}

	@Override
	public Validator create(CreationalContext<Validator> ctx) {
		ValidatorFactory validatorFactory = createValidatorFactory();
		return validatorFactory.getValidator();
	}

	private ValidatorFactory createValidatorFactory() {
		CreationalContext<?> context = beanManager.createCreationalContext( validatorFactoryBean );
		return (ValidatorFactory) beanManager.getReference( validatorFactoryBean, ValidatorFactory.class, context );
	}

	@Override
	public void destroy(Validator instance, CreationalContext<Validator> ctx) {
	}

	@Override
	public String getId() {
		return ValidatorBean.class.getName() + "_" + ( validationProviderHelper.isDefaultProvider() ? "default" : "hv" );
	}

	@Override
	public String toString() {
		return "ValidatorBean [id=" + getId() + ", qualifiers=" + getQualifiers() + "]";
	}
}
