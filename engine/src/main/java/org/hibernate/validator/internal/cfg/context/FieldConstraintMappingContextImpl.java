/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An implementation of {@link AbstractPropertyConstraintMappingContextImpl} for a field property.
 * Represents a constraint mapping creational context which allows to configure the constraints
 * for one of the bean's field properties.
 *
 * @author Marko Bekhta
 */
final class FieldConstraintMappingContextImpl extends AbstractPropertyConstraintMappingContextImpl {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Field field;

	FieldConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Field field) {
		super( typeContext, ReflectionHelper.typeOf( field ) );
		this.field = field;
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
		JavaBeanField resolved = helper.field( field );
		if ( !resolvedMembers.add( resolved ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( typeContext.getBeanClass(), field.getName() );
		}
		ConstraintLocation location = ConstraintLocation.forField( resolved );
		if ( ignoreAnnotations != null ) {
			options.ignoreConstraintAnnotationsOnMember( resolved, ignoreAnnotations );
		}
		return new ResolvedConstraintMapping.ResolvedField( resolved, materializeConstraints( location ),
				materializeCascading( location, resolved.getType() ) );
	}
}
