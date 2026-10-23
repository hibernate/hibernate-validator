/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Gunnar Morling
 *
 */
public class ContainerElementConstraintMappingContextImpl extends CascadableConstraintMappingContextImplBase<ContainerElementConstraintMappingContext>
		implements ContainerElementConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final ContainerElementTarget parentContainerElementTarget;
	private final int index;
	private final Map<Integer, ContainerElementConstraintMappingContextImpl> nestedContainerElementContexts = new HashMap<>();

	ContainerElementConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, ContainerElementTarget parentContainerElementTarget,
			Type containerType, int index) {
		super( typeContext.getConstraintMapping(), containerType );
		this.typeContext = typeContext;
		this.parentContainerElementTarget = parentContainerElementTarget;
		this.index = index;
		if ( containerType != null ) {
			// Only called to fail fast on an out-of-bounds index; the element type itself is resolved lazily, as for
			// getters it is not known before the destination factory has resolved the getter.
			getContainerElementType( containerType, index );
		}
	}

	@Override
	protected ContainerElementConstraintMappingContext getThis() {
		return this;
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
		return parentContainerElementTarget.containerElementType();
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return parentContainerElementTarget.containerElementType( index, nestedIndexes );
	}

	ContainerElementConstraintMappingContext nestedContainerElement(int[] nestedIndexes) {
		ContainerElementConstraintMappingContextImpl nestedContext = nestedContainerElementContexts.get( nestedIndexes[0] );
		if ( nestedContext == null ) {
			Type elementType = configuredType == null ? null : getContainerElementType( configuredType, index );
			nestedContext = new ContainerElementConstraintMappingContextImpl( typeContext, parentContainerElementTarget, elementType, nestedIndexes[0] );
			nestedContainerElementContexts.put( nestedIndexes[0], nestedContext );
		}
		return nestedIndexes.length == 1 ? nestedContext : nestedContext.nestedContainerElement( Arrays.copyOfRange( nestedIndexes, 1, nestedIndexes.length ) );
	}

	@Override
	public ContainerElementConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		addConstraint( definition );
		return this;
	}

	private static Type getContainerElementType(Type containerType, int index) {
		checkContainerType( containerType );
		Type[] arguments = ( (ParameterizedType) containerType ).getActualTypeArguments();
		if ( index >= arguments.length ) {
			throw LOG.getInvalidTypeArgumentIndexException( containerType, index );
		}
		return arguments[index];
	}

	ResolvedConstraintMapping.ResolvedCascading materialize(ConstraintLocation parentLocation) {
		Type containerType = parentLocation.getTypeForValidatorResolution();
		Type elementType = getContainerElementType( containerType, index );
		TypeVariable<?> parameter = ReflectionHelper.getClassFromType( containerType ).getTypeParameters()[index];
		ConstraintLocation location = ConstraintLocation.forTypeArgument( parentLocation, parameter, elementType );
		Set<ConfiguredConstraint<?>> constraints = materializeConstraints( location );
		Map<TypeVariable<?>, CascadingMetaDataBuilder> cascading = new HashMap<>();
		for ( ContainerElementConstraintMappingContextImpl child : nestedContainerElementContexts.values() ) {
			ResolvedConstraintMapping.ResolvedCascading resolved = child.materialize( location );
			constraints.addAll( resolved.containerConstraints );
			cascading.put( resolved.cascading.getTypeParameter(), resolved.cascading );
		}
		return new ResolvedConstraintMapping.ResolvedCascading( constraints,
				new CascadingMetaDataBuilder( containerType, parameter, isCascading, cascading, new HashMap<>( groupConversions ) ) );
	}
}
