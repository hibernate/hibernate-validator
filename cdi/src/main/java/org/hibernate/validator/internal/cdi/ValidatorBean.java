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
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class ValidatorBean implements Bean<Validator>, PassivationCapable {
	private final BeanManager beanManager;
	private final Set<Annotation> qualifiers;

	public ValidatorBean(BeanManager beanManager, Set<Annotation> qualifiers) {
		this.beanManager = beanManager;
		this.qualifiers = CollectionHelper.newHashSet();
		this.qualifiers.addAll( qualifiers );
		this.qualifiers.add(
				new AnnotationLiteral<Any>() {
				}
		);
	}

	@Override
	public Class<?> getBeanClass() {
		return ValidatorImpl.class;
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
		Set<Type> types = new HashSet<Type>();

		types.add( Validator.class );
		types.add( ExecutableValidator.class );
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
	public Validator create(CreationalContext<Validator> ctx) {
		ValidatorFactory validatorFactory = getReference( beanManager, ValidatorFactory.class );
		return validatorFactory.getValidator();
	}

	@Override
	public void destroy(Validator instance, CreationalContext<Validator> ctx) {
	}

	@SuppressWarnings("unchecked")
	private <T> T getReference(BeanManager beanManager, Class<T> clazz) {
		Set<Bean<?>> beans = beanManager.getBeans( clazz, qualifiers.toArray( new Annotation[qualifiers.size()] ) );
		for ( Bean<?> bean : beans ) {
			for ( Annotation annotation : bean.getQualifiers() ) {
				if ( annotation.annotationType().getName().equals( HibernateValidator.class.getName() ) ) {
					CreationalContext<?> context = beanManager.createCreationalContext( bean );
					return (T) beanManager.getReference( bean, clazz, context );
				}
			}
		}
		return null;
	}

    @Override
    public String getId() {
        return ValidatorBean.class.getName();
    }
}
