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
import java.lang.invoke.MethodHandles;
import java.util.Set;

import jakarta.validation.Constraint;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
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

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final JavaBeanHelper javaBeanHelper;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Set<Class<?>> configuredTypes;
	private final Set<TypeConstraintMappingContextImpl<?>> typeContexts;
	private final Set<Class<?>> definedConstraints;
	private final Set<ConstraintDefinitionContextImpl<?>> constraintContexts;

	public DefaultConstraintMapping(JavaBeanHelper javaBeanHelper) {
		this.javaBeanHelper = javaBeanHelper;
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
			throw LOG.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( type );
		}

		TypeConstraintMappingContextImpl<C> typeContext = new TypeConstraintMappingContextImpl<>( javaBeanHelper, this, type );
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
	 * @param constraintCreationContext the constraint creation context
	 *
	 * @return a set of {@link BeanConfiguration}s with an element for each type configured through this mapping
	 */
	public Set<BeanConfiguration<?>> getBeanConfigurations(ConstraintCreationContext constraintCreationContext) {
		Set<BeanConfiguration<?>> configurations = newHashSet();

		for ( TypeConstraintMappingContextImpl<?> typeContext : typeContexts ) {
			configurations.add( typeContext.build( constraintCreationContext ) );
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
			throw LOG.getConstraintHasAlreadyBeenConfiguredViaProgrammaticApiException( annotationClass );
		}

		ConstraintDefinitionContextImpl<A> constraintContext = new ConstraintDefinitionContextImpl<>( this, annotationClass );
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
