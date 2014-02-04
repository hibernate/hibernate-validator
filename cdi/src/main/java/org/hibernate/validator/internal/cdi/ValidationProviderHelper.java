/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValidatorImpl;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Provides functionality for dealing with validation provider types.
 *
 * @author Gunnar Morling
 */
public class ValidationProviderHelper {

	private final boolean isDefaultProvider;
	private final boolean isHibernateValidator;
	private final Class<? extends ValidatorFactory> validatorFactoryClass;
	private final Class<? extends Validator> validatorClass;
	private final Set<Annotation> qualifiers;

	public static ValidationProviderHelper forDefaultProvider(ValidatorFactory validatorFactory) {
		boolean isHibernateValidator = validatorFactory instanceof HibernateValidatorFactory;

		return new ValidationProviderHelper(
				true,
				isHibernateValidator,
				validatorFactory.getClass(),
				validatorFactory.getValidator().getClass(),
				determineRequiredQualifiers( true, isHibernateValidator )
		);
	}

	public static ValidationProviderHelper forHibernateValidator() {
		return new ValidationProviderHelper(
				false,
				true,
				ValidatorFactoryImpl.class,
				ValidatorImpl.class,
				determineRequiredQualifiers( false, true )
		);
	}

	private ValidationProviderHelper(boolean isDefaultProvider,
			boolean isHibernateValidator,
			Class<? extends ValidatorFactory> validatorFactoryClass,
			Class<? extends Validator> validatorClass,
			Set<Annotation> qualifiers) {
		this.isDefaultProvider = isDefaultProvider;
		this.isHibernateValidator = isHibernateValidator;
		this.validatorFactoryClass = validatorFactoryClass;
		this.validatorClass = validatorClass;
		this.qualifiers = Collections.unmodifiableSet( qualifiers );
	}

	/**
	 * Whether the given provider is the default provider or not.
	 */
	public boolean isDefaultProvider() {
		return isDefaultProvider;
	}

	/**
	 * Whether the given provider is Hibernate Validator or not.
	 */
	boolean isHibernateValidator() {
		return isHibernateValidator;
	}

	/**
	 * Determines the class of the {@link ValidatorFactory} corresponding to the given configuration object.
	 */
	Class<? extends ValidatorFactory> getValidatorFactoryBeanClass() {
		return validatorFactoryClass;
	}

	/**
	 * Determines the class of the {@link Validator} corresponding to the given configuration object.
	 */
	Class<? extends Validator> getValidatorBeanClass() {
		return validatorClass;
	}

	Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	/**
	 * Returns the qualifiers to be used for registering a validator or validator factory.
	 */
	@SuppressWarnings("serial")
	private static Set<Annotation> determineRequiredQualifiers(boolean isDefaultProvider,
			boolean isHibernateValidator) {
		HashSet<Annotation> qualifiers = newHashSet( 3 );

		if ( isDefaultProvider ) {
			qualifiers.add(
					new AnnotationLiteral<Default>() {
					}
			);
		}

		if ( isHibernateValidator ) {
			qualifiers.add(
					new AnnotationLiteral<HibernateValidator>() {
					}
			);
		}

		qualifiers.add(
				new AnnotationLiteral<Any>() {
				}
		);

		return qualifiers;
	}

	@Override
	public String toString() {
		return "ValidationProviderHelper [isDefaultProvider="
				+ isDefaultProvider + ", isHibernateValidator="
				+ isHibernateValidator + ", validatorFactoryClass="
				+ validatorFactoryClass + ", validatorClass=" + validatorClass
				+ ", qualifiers=" + qualifiers + "]";
	}
}
