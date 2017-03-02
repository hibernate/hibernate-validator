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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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

	private static final Log LOG = LoggerFactory.make();

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final Member member;
	private final ConstraintLocation location;
	private final Type validatedType;

	PropertyConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Member member) {
		super( typeContext.getConstraintMapping() );
		this.typeContext = typeContext;
		this.member = member;
		if ( member instanceof Field ) {
			this.location = ConstraintLocation.forField( (Field) member );
			this.validatedType = ( (Field) member ).getGenericType();
		}
		else {
			this.location = ConstraintLocation.forGetter( (Method) member );
			this.validatedType = ( (Method) member ).getGenericReturnType();
		}
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

	@Override
	public ContainerElementConstraintMappingContext containerElement() {
		if ( validatedType instanceof ParameterizedType ) {
			if ( ( (ParameterizedType) validatedType ).getActualTypeArguments().length > 1 ) {
				throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( validatedType );
			}
		}
		else if ( !TypeHelper.isArray( validatedType ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( validatedType );
		}

		return (containerElement( 0 ) );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElement(int index, int... nestedIndexes) {
		if ( !( validatedType instanceof ParameterizedType ) && !( TypeHelper.isArray( validatedType ) ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( validatedType );
		}

		return super.containerElement( this, typeContext, location, index, nestedIndexes );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		// TODO HV-919 Support specification of type parameter constraints via XML and API
		if ( member instanceof Field ) {
			return new ConstrainedField(
					ConfigurationSource.API,
					(Field) member,
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					Collections.emptySet(),
					groupConversions,
					getCascadedTypeParameters( (Field) member, isCascading )
			);
		}
		else {
			return new ConstrainedExecutable(
					ConfigurationSource.API,
					(Executable) member,
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
					groupConversions,
					getCascadedTypeParameters( (Executable) member, isCascading )
			);
		}
	}

	private List<CascadingTypeParameter> getCascadedTypeParameters(Field field, boolean isCascaded) {
		if ( isCascaded ) {
			return Collections.singletonList( field.getType().isArray()
					? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( field ) )
					: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( field ) ) );
		}
		else {
			return Collections.emptyList();
		}
	}

	private List<CascadingTypeParameter> getCascadedTypeParameters(Executable executable, boolean isCascaded) {
		List<CascadingTypeParameter> cascadingTypeParameters = new ArrayList<>();

		for ( ContainerElementConstraintMappingContextImpl typeArgumentContext : containerElementContexts.values() ) {
			CascadingTypeParameter cascadingTypeParameter = typeArgumentContext.getCascadingTypeParameter();
			if ( cascadingTypeParameter != null ) {
				cascadingTypeParameters.add( cascadingTypeParameter );
			}
		}

		if ( isCascaded ) {
			boolean isArray = executable instanceof Method && ( (Method) executable ).getReturnType().isArray();
			cascadingTypeParameters.add( isArray
					? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( executable ) )
					: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( executable ) ) );
		}

		return cascadingTypeParameters;
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
