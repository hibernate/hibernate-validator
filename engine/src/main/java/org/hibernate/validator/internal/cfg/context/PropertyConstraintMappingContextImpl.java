/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
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

	// either Field or Method
	private final Property property;
	private final ConstraintLocation location;

	PropertyConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Property property) {
		super( typeContext.getConstraintMapping(), property.getType() );
		this.typeContext = typeContext;
		this.property = property;
		this.location = property instanceof JavaBeanField
				? ConstraintLocation.forField( property.as( JavaBeanField.class ) )
				: ConstraintLocation.forGetter( property.as( JavaBeanGetter.class ) );
	}

	@Override
	protected PropertyConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		if ( property instanceof JavaBeanField ) {
			super.addConstraint(
					ConfiguredConstraint.forProperty(
							definition, property
					)
			);
		}
		else {
			super.addConstraint(
					ConfiguredConstraint.forExecutable(
							definition, property.as( Callable.class )
					)
			);
		}
		return this;
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations() {
		return ignoreAnnotations( true );
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnMember( property, ignoreAnnotations );
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

	@Override
	public ContainerElementConstraintMappingContext containerElementType() {
		return super.containerElement( this, typeContext, location );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return super.containerElement( this, typeContext, location, index, nestedIndexes );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		if ( property instanceof JavaBeanField ) {
			return new ConstrainedField(
					ConfigurationSource.API,
					property,
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					getCascadingMetaDataBuilder()
			);
		}
		else {
			return new ConstrainedExecutable(
					ConfigurationSource.API,
					property.as( Callable.class ),
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					getCascadingMetaDataBuilder()
			);
		}
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
