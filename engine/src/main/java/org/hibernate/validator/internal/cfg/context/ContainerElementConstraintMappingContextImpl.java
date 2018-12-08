/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementTarget;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterTarget;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueTarget;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Gunnar Morling
 *
 */
public class ContainerElementConstraintMappingContextImpl extends CascadableConstraintMappingContextImplBase<ContainerElementConstraintMappingContext> implements ContainerElementConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final ContainerElementTarget parentContainerElementTarget;
	private final ConstraintLocation parentLocation;

	/**
	 * The type configured through this context. Either a {@code ParameterizedType} or an array type.
	 */
	private final Type configuredType;

	/**
	 * The index of the type parameter configured through this context. Always 0 in case of an array type.
	 */
	private final int index;

	/**
	 * The type parameter configured through this context. An instance of {@link ArrayElement} in case of an array type.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * Contexts for configuring nested container elements, if any. Indexed by type parameter.
	 */
	protected final Map<Integer, ContainerElementConstraintMappingContextImpl> nestedContainerElementContexts;

	private final Set<ConfiguredConstraint<?>> constraints;

	ContainerElementConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, ContainerElementTarget parentContainerElementTarget,
			ConstraintLocation parentLocation, int index) {
		super( typeContext.getConstraintMapping(), parentLocation.getTypeForValidatorResolution() );
		this.typeContext = typeContext;
		this.parentContainerElementTarget = parentContainerElementTarget;
		this.parentLocation = parentLocation;
		this.configuredType = parentLocation.getTypeForValidatorResolution();

		// HV-1428 Container element support is disabled for arrays
		if ( TypeHelper.isArray( configuredType ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( configuredType );
		}

		if ( configuredType instanceof ParameterizedType ) {
			TypeVariable<?>[] typeParameters = ReflectionHelper.getClassFromType( configuredType ).getTypeParameters();

			if ( index > typeParameters.length - 1 ) {
				throw LOG.getInvalidTypeArgumentIndexException( configuredType, index );
			}
			else {
				this.typeParameter = typeParameters[index];
			}
		}
		else {
			typeParameter = new ArrayElement( configuredType );
		}

		this.index = index;
		this.constraints = new HashSet<>();
		this.nestedContainerElementContexts = new HashMap<>();
	}

	@Override
	protected ContainerElementConstraintMappingContext getThis() {
		return this;
	}

	@Override
	@Deprecated
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		return typeContext.property( property, elementType );
	}

	@Override
	public PropertyConstraintMappingContext field(String property) {
		return typeContext.field( property );
	}

	@Override
	public PropertyConstraintMappingContext getter(String property) {
		return typeContext.getter( property );
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
	public ParameterConstraintMappingContext parameter(int index) {
		if ( parentContainerElementTarget instanceof ParameterTarget ) {
			return ( (ParameterTarget) parentContainerElementTarget ).parameter( index );
		}
		else {
			throw LOG.getParameterIsNotAValidCallException();
		}
	}

	@Override
	public ReturnValueConstraintMappingContext returnValue() {
		if ( parentContainerElementTarget instanceof ReturnValueTarget ) {
			return ( (ReturnValueTarget) parentContainerElementTarget ).returnValue();
		}
		else {
			throw LOG.getReturnValueIsNotAValidCallException();
		}
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType() {
		return parentContainerElementTarget.containerElementType( 0 );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return parentContainerElementTarget.containerElementType( index, nestedIndexes );
	}

	ContainerElementConstraintMappingContext nestedContainerElement(int[] nestedIndexes) {
		if ( !( configuredType instanceof ParameterizedType ) && !( TypeHelper.isArray( configuredType ) ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( configuredType );
		}

		ContainerElementConstraintMappingContextImpl nestedContext = nestedContainerElementContexts.get( nestedIndexes[0] );
		if ( nestedContext == null ) {
			nestedContext = new ContainerElementConstraintMappingContextImpl(
					typeContext,
					parentContainerElementTarget,
					ConstraintLocation.forTypeArgument( parentLocation, typeParameter, getContainerElementType() ),
					nestedIndexes[0]
			);
			nestedContainerElementContexts.put( nestedIndexes[0], nestedContext );
		}

		if ( nestedIndexes.length > 1 ) {
			return nestedContext.nestedContainerElement( Arrays.copyOfRange( nestedIndexes, 1, nestedIndexes.length ) );
		}
		else {
			return nestedContext;
		}
	}

	@Override
	public ContainerElementConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		constraints.add( ConfiguredConstraint.forTypeArgument( definition, parentLocation, typeParameter, getContainerElementType() ) );
		return this;
	}

	private Type getContainerElementType() {
		if ( configuredType instanceof ParameterizedType ) {
			return ( (ParameterizedType) configuredType ).getActualTypeArguments()[index];
		}
		else {
			return TypeHelper.getComponentType( configuredType );
		}
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}

	CascadingMetaDataBuilder getContainerElementCascadingMetaDataBuilder() {
		return new CascadingMetaDataBuilder(
			parentLocation.getTypeForValidatorResolution(),
			typeParameter,
			isCascading,
			nestedContainerElementContexts.values()
					.stream()
					.map( ContainerElementConstraintMappingContextImpl::getContainerElementCascadingMetaDataBuilder )
					.collect( Collectors.toMap( CascadingMetaDataBuilder::getTypeParameter, Function.identity() ) ),
			groupConversions
		);
	}

	Set<MetaConstraint<?>> build(ConstraintCreationContext constraintCreationContext) {
		return Stream.concat(
			constraints.stream()
				.map( c -> asMetaConstraint( c, constraintCreationContext ) ),
			nestedContainerElementContexts.values()
				.stream()
				.map( c -> c.build( constraintCreationContext ) )
				.flatMap( Set::stream )
			)
			.collect( Collectors.toSet() );
	}

	private <A extends Annotation> MetaConstraint<A> asMetaConstraint(ConfiguredConstraint<A> config, ConstraintCreationContext constraintCreationContext) {
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<>(
				constraintCreationContext.getConstraintHelper(),
				config.getLocation().getConstrainable(),
				config.createAnnotationDescriptor(),
				config.getLocation().getKind(),
				getConstraintType()
		);

		return MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(), constraintCreationContext.getValueExtractorManager(),
				constraintCreationContext.getConstraintValidatorManager(), constraintDescriptor, config.getLocation() );
	}

	@Override
	public String toString() {
		return "TypeArgumentConstraintMappingContextImpl [configuredType=" + StringHelper.toShortString( configuredType ) + ", typeParameter=" + typeParameter
				+ "]";
	}
}
