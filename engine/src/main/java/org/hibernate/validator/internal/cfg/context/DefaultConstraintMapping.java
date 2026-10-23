/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Reusable programmatic declarations. Fluent contexts retain reflection members and configuration;
 * factory-specific members and constraint locations are created only during materialization.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class DefaultConstraintMapping implements ConstraintMapping {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<Class<?>> configuredTypes;
	private final Set<TypeConstraintMappingContextImpl<?>> typeContexts;
	private final Set<Class<?>> definedConstraints;
	private final Set<ConstraintDefinitionContextImpl<?>> constraintContexts;

	public DefaultConstraintMapping() {
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

		TypeConstraintMappingContextImpl<C> typeContext = new TypeConstraintMappingContextImpl<>( this, type );
		typeContexts.add( typeContext );
		configuredTypes.add( type );

		return typeContext;
	}

	/** Creates factory-owned locations and options without modifying the declarations. */
	public ResolvedConstraintMapping materialize(JavaBeanHelper javaBeanHelper) {
		AnnotationProcessingOptionsImpl options = new AnnotationProcessingOptionsImpl();
		Set<ResolvedConstraintMapping.ResolvedType<?>> types = newHashSet();
		for ( TypeConstraintMappingContextImpl<?> context : typeContexts ) {
			types.add( context.materialize( javaBeanHelper, options ) );
		}
		return new ResolvedConstraintMapping( types, options, getConstraintDefinitionContributions() );
	}

	@Override
	public final <A extends Annotation> ConstraintDefinitionContext<A> constraintDefinition(Class<A> annotationClass) {
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
