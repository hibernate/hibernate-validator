/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementTarget;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Gunnar Morling
 *
 */
public class ContainerElementConstraintMappingContextImpl extends CascadableConstraintMappingContextImplBase<ContainerElementConstraintMappingContext> implements ContainerElementConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make();

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final ContainerElementTarget parent;
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
	 * The type parameter configured through this context. {@link ArrayElement#INSTANCE} in case of an array type.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * Contexts for configuring nested container elements, if any. Indexed by type parameter.
	 */
	protected final Map<Integer, ContainerElementConstraintMappingContextImpl> nestedContainerElementContexts;

	private final Set<ConfiguredConstraint<?>> constraints;

	ContainerElementConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, ContainerElementTarget parent, ConstraintLocation parentLocation,
			int index) {
		super( typeContext.getConstraintMapping() );
		this.typeContext = typeContext;
		this.parent = parent;
		this.parentLocation = parentLocation;
		this.configuredType = parentLocation.getTypeForValidatorResolution();

		if ( parentLocation.getTypeForValidatorResolution() instanceof ParameterizedType ) {
			TypeVariable<?>[] typeParameters = ReflectionHelper.getClassFromType( configuredType ).getTypeParameters();

			if ( index > typeParameters.length - 1 ) {
				throw LOG.getInvalidTypeArgumentIndexException( configuredType, index );
			}
			else {
				this.typeParameter = typeParameters[index];
			}
		}
		else {
			typeParameter = new ArrayElement( (Class<?>) configuredType );
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
		return parent.containerElement( 0 );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElement(int index, int... nestedIndexes) {
		return parent.containerElement( index, nestedIndexes );
	}

	ContainerElementConstraintMappingContext nestedContainerElement(int[] nestedIndexes) {
		if ( !( configuredType instanceof ParameterizedType ) && !( TypeHelper.isArray( configuredType ) ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException(  configuredType );
		}

		ContainerElementConstraintMappingContextImpl nestedContext = new ContainerElementConstraintMappingContextImpl(
			typeContext,
			parent,
			ConstraintLocation.forTypeArgument( parentLocation, typeParameter, getContainerElementType() ),
			nestedIndexes[0]
		);

		nestedContainerElementContexts.put( nestedIndexes[0], nestedContext );

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
			return ( (Class<?>) configuredType ).getComponentType();
		}
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}

	CascadingTypeParameter getCascadingTypeParameter() {
		return new CascadingTypeParameter(
			parentLocation.getTypeForValidatorResolution(),
			typeParameter,
			isCascading,
			nestedContainerElementContexts.values()
				.stream()
				.map( ContainerElementConstraintMappingContextImpl::getCascadingTypeParameter )
				.collect( Collectors.toList() )
		);
	}

	Set<MetaConstraint<?>> build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		return Stream.concat(
			constraints.stream()
				.map( c -> asMetaConstraint( c, constraintHelper, typeResolutionHelper, valueExtractorManager ) ),
			nestedContainerElementContexts.values()
				.stream()
				.map( c -> c.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) )
				.flatMap( Set::stream )
			)
			.collect( Collectors.toSet() );
	}

	private <A extends Annotation> MetaConstraint<A> asMetaConstraint(ConfiguredConstraint<A> config, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<>(
				constraintHelper,
				config.getLocation().getMember(),
				config.createAnnotationProxy(),
				config.getElementType(),
				getConstraintType()
		);

		return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescriptor, config.getLocation() );
	}

	@Override
	public String toString() {
		return "TypeArgumentConstraintMappingContextImpl [configuredType=" + StringHelper.toShortString( configuredType ) + ", typeParameter=" + typeParameter
				+ "]";
	}
}
