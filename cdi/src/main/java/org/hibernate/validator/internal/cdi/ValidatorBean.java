/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;

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

	public ValidatorBean(BeanManager beanManager, Bean<?> validatorFactoryBean,
			ValidationProviderHelper validationProviderHelper) {
		this.beanManager = beanManager;
		this.validatorFactoryBean = validatorFactoryBean;
		this.validationProviderHelper = validationProviderHelper;
		this.types = Collections.unmodifiableSet(
				CollectionHelper.<Type>newHashSet(
						ClassHierarchyHelper.getHierarchy( validationProviderHelper.getValidatorBeanClass() )
				)
		);
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

	@Override
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
		return "ValidatorBean [id=" + getId() + "]";
	}
}
