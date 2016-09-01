/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Default implementation of {@link ConstraintMapping}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class DefaultConstraintMapping implements ConstraintMapping {

	private static final Log log = LoggerFactory.make();

	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Set<Class<?>> configuredTypes;
	private final Set<TypeConstraintMappingContextImpl<?>> typeContexts;
	private final Set<Class<?>> definedConstraints;
	private final Set<ConstraintDefinitionContextImpl<?>> constraintContexts;

	public DefaultConstraintMapping() {
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.configuredTypes = newHashSet();
		this.typeContexts = newHashSet();
		this.definedConstraints = newHashSet();
		this.constraintContexts = newHashSet();
	}

	@Override
	public final <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		Contracts.assertNotNull( type, MESSAGES.beanTypeMustNotBeNull() );

		if ( configuredTypes.contains( type ) ) {
			throw log.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( type );
		}

		TypeConstraintMappingContextImpl<C> typeContext = new TypeConstraintMappingContextImpl<C>( this, type );
		typeContexts.add( typeContext );
		configuredTypes.add( type );

		return typeContext;
	}

	public final AnnotationProcessingOptionsImpl getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public Set<Class<?>> getConfiguredTypes() {
		return configuredTypes;
	}

	/**
	 * Returns all bean configurations configured through this constraint mapping.
	 *
	 * @param constraintHelper constraint helper required for building constraint descriptors
	 * @param parameterNameProvider parameter name provider required for building parameter elements
	 *
	 * @return a set of {@link BeanConfiguration}s with an element for each type configured through this mapping
	 */
	public Set<BeanConfiguration<?>> getBeanConfigurations(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		Set<BeanConfiguration<?>> configurations = newHashSet();

		for ( TypeConstraintMappingContextImpl<?> typeContext : typeContexts ) {
			configurations.add( typeContext.build( constraintHelper, parameterNameProvider ) );
		}

		return configurations;
	}

	@Override
	public <A extends Annotation> ConstraintDefinitionContext<A> constraintDefinition(Class<A> annotationClass) {
		Contracts.assertNotNull( annotationClass, MESSAGES.annotationTypeMustNotBeNull() );
		Contracts.assertTrue( annotationClass.isAnnotationPresent( Constraint.class ),
				MESSAGES.annotationTypeMustBeAnnotatedWithConstraint() );

		if ( definedConstraints.contains( annotationClass ) ) {
			// Fail fast for easy-to-detect definition conflicts; other conflicts are handled in ValidatorFactoryImpl
			throw log.getConstraintHasAlreadyBeenConfiguredViaProgrammaticApiException( annotationClass );
		}

		ConstraintDefinitionContextImpl<A> constraintContext = new ConstraintDefinitionContextImpl<A>( this, annotationClass );
		constraintContexts.add( constraintContext );
		definedConstraints.add( annotationClass );

		return constraintContext;
	}

	public Set<ConstraintDefinitionContribution<?>> getConstraintDefinitionContributions() {
		Set<ConstraintDefinitionContribution<?>> contributions = newHashSet();

		for ( ConstraintDefinitionContextImpl<?> constraintContext : constraintContexts ) {
			contributions.add( constraintContext.build() );
		}

		return contributions;
	}
}
