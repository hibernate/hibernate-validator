/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.internal;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filter;

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

	private ValidationProviderHelper(
			boolean isDefaultProvider,
			boolean isHibernateValidator,
			Class<? extends ValidatorFactory> validatorFactoryClass,
			Class<? extends Validator> validatorClass,
			Set<Annotation> qualifiers
	) {
		this.isDefaultProvider = isDefaultProvider;
		this.isHibernateValidator = isHibernateValidator;
		this.validatorFactoryClass = validatorFactoryClass;
		this.validatorClass = validatorClass;
		this.qualifiers = Collections.unmodifiableSet( qualifiers );
	}

	/**
	 * Whether the given provider is the default provider or not.
	 *
	 * @return {@code true} if the given provider is the default provider, {@code false} otherwise
	 */
	public boolean isDefaultProvider() {
		return isDefaultProvider;
	}

	/**
	 * Whether the given provider is Hibernate Validator or not.
	 */
	public boolean isHibernateValidator() {
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
	private static Set<Annotation> determineRequiredQualifiers(
			boolean isDefaultProvider,
			boolean isHibernateValidator
	) {
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

	public Set<Type> determineValidatorFactoryCdiTypes() {
		return Collections.unmodifiableSet(
				CollectionHelper.<Type>newHashSet(
						ClassHierarchyHelper.getHierarchy(
								getValidatorFactoryBeanClass(),
								isHibernateValidator ? HibernateValidatorInternalTypeFilter.INSTANCE : AcceptAllFilter.INSTANCE
						)
				)
		);
	}

	public Set<Type> determineValidatorCdiTypes() {
		return Collections.unmodifiableSet(
				CollectionHelper.<Type>newHashSet(
						ClassHierarchyHelper.getHierarchy(
								getValidatorBeanClass(),
								isHibernateValidator ? HibernateValidatorInternalTypeFilter.INSTANCE : AcceptAllFilter.INSTANCE
						)
				)
		);
	}

	private static class HibernateValidatorInternalTypeFilter implements Filter {

		public static final HibernateValidatorInternalTypeFilter INSTANCE = new HibernateValidatorInternalTypeFilter();

		@Override
		public boolean accepts(Class<?> clazz) {
			return !isHibernateValidatorInternalType( clazz );
		}
	}

	private static class AcceptAllFilter implements Filter {

		public static final AcceptAllFilter INSTANCE = new AcceptAllFilter();

		@Override
		public boolean accepts(Class<?> clazz) {
			return true;
		}
	}

	@Override
	public String toString() {
		return "ValidationProviderHelper [isDefaultProvider="
				+ isDefaultProvider + ", isHibernateValidator="
				+ isHibernateValidator + ", validatorFactoryClass="
				+ validatorFactoryClass + ", validatorClass=" + validatorClass
				+ ", qualifiers=" + qualifiers + "]";
	}

	private static boolean isHibernateValidatorInternalType(Class<?> klass) {
		return klass.getPackageName().startsWith( "org.hibernate.validator." )
				&& ( klass.getPackageName().endsWith( ".internal" )
						|| klass.getPackageName().contains( ".internal." ) );
	}
}
