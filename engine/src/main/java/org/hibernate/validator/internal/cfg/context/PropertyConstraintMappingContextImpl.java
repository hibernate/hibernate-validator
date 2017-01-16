/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.engine.cascading.ValueExtractors;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
final class PropertyConstraintMappingContextImpl
		extends CascadableConstraintMappingContextImplBase<PropertyConstraintMappingContext>
		implements PropertyConstraintMappingContext {

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final Member member;

	PropertyConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Member member) {
		super( typeContext.getConstraintMapping() );
		this.typeContext = typeContext;
		this.member = member;
	}

	@Override
	protected PropertyConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		if ( member instanceof Field ) {
			super.addConstraint(
					ConfiguredConstraint.forProperty(
							definition, member
					)
			);
		}
		else {
			super.addConstraint(
					ConfiguredConstraint.forExecutable(
							definition, (Method) member
					)
			);
		}
		return this;
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnMember( member, ignoreAnnotations );
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		return typeContext.property( property, elementType );
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		return typeContext.constructor( parameterTypes );
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return typeContext.method( name, parameterTypes );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractors valueExtractors) {
		// TODO HV-919 Support specification of type parameter constraints via XML and API
		if ( member instanceof Field ) {
			return new ConstrainedField(
					ConfigurationSource.API,
					(Field) member,
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractors ),
					Collections.emptySet(),
					groupConversions,
					getCascadedTypeParameters( (Field) member, isCascading )
			);
		}
		else {
			return new ConstrainedExecutable(
					ConfigurationSource.API,
					(Executable) member,
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractors ),
					groupConversions,
					getCascadedTypeParameters( (Executable) member, isCascading )
			);
		}
	}

	private List<TypeVariable<?>> getCascadedTypeParameters(Field field, boolean isCascaded) {
		if ( isCascaded ) {
			return Collections.singletonList( field.getType().isArray() ? ArrayElement.INSTANCE : AnnotatedObject.INSTANCE );
		}
		else {
			return Collections.emptyList();
		}
	}

	private List<TypeVariable<?>> getCascadedTypeParameters(Executable executable, boolean isCascaded) {
		if ( isCascaded ) {
			boolean isArray = executable instanceof Method && ( (Method) executable ).getReturnType().isArray();
			return Collections.singletonList( isArray ? ArrayElement.INSTANCE : AnnotatedObject.INSTANCE );
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
