/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An implementation of {@link AbstractPropertyConstraintMappingContextImpl} for a getter property.
 * Represents a constraint mapping creational context which allows to configure the constraints
 * for one of the bean's getter properties.
 *
 * @author Marko Bekhta
 */
final class GetterConstraintMappingContextImpl extends AbstractPropertyConstraintMappingContextImpl {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String property;

	GetterConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, String property) {
		super( typeContext, null );
		this.property = property;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		addConstraint( definition );
		return this;
	}

	@Override
	public ResolvedConstraintMapping.ResolvedElement materialize(JavaBeanHelper helper,
			AnnotationProcessingOptionsImpl options,
			Set<Constrainable> resolvedMembers) {
		JavaBeanGetter resolved = helper.findDeclaredGetter( typeContext.getBeanClass(), property ).orElseThrow(
				() -> LOG.getUnableToFindPropertyWithAccessException( typeContext.getBeanClass(), property, ElementType.METHOD ) );
		if ( !resolvedMembers.add( resolved ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( typeContext.getBeanClass(), property );
		}
		ConstraintLocation location = ConstraintLocation.forGetter( resolved );
		if ( ignoreAnnotations != null ) {
			options.ignoreConstraintAnnotationsOnMember( resolved, ignoreAnnotations );
		}
		return new ResolvedConstraintMapping.ResolvedExecutable( resolved, List.of(), Set.of(),
				materializeConstraints( ConstraintLocation.forReturnValue( resolved ) ), materializeCascading( location, resolved.getType() ) );
	}
}
