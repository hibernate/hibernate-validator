/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.propertyholder.CascadableContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.ContainerElementTarget;
import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.cascading.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Context for simple container elements that are not property holders themselve. Hence no cascading on them should be allowed.
 *
 * @author Marko Bekhta
 */
public class ContainerElementConstraintMappingContextImpl implements
		ContainerElementConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final PropertyHolderTypeConstraintMappingContextImpl typeContext;
	private final ContainerElementTarget parentContainerElementTarget;

	/**
	 * The type configured through this context. Either a {@code ParameterizedType} or an array type.
	 */
	private final Class<?> configuredType;

	/**
	 * The index of the type parameter configured through this context. Always 0 in case of an array type.
	 */
	private final int index;

	/**
	 * The type parameter configured through this context. An instance of {@link ArrayElement} in case of an array type.
	 */
	private final TypeVariable<?> typeParameter;

	private final Class<?> containerElementType;

	/**
	 * Contexts for configuring nested container elements, if any. Indexed by type parameter.
	 */
	protected final Map<Integer, ContainerElementConstraintMappingContextImpl> nestedContainerElementContexts;

	private final Set<MetaConstraintBuilder<?>> constraints;

	ContainerElementConstraintMappingContextImpl(
			PropertyHolderTypeConstraintMappingContextImpl typeContext,
			ContainerElementTarget parentContainerElementTarget,
			Class<?> type,
			int index,
			Class<?> containerElementType) {
		this.typeContext = typeContext;
		this.parentContainerElementTarget = parentContainerElementTarget;

		// HV-1428 Container element support is disabled for arrays
		if ( type.isArray() ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( type );
		}

		TypeVariable<?>[] typeParameters = type.getTypeParameters();

		if ( index > typeParameters.length - 1 ) {
			throw LOG.getInvalidTypeArgumentIndexException( type, index );
		}
		else {
			this.typeParameter = typeParameters[index];
		}

		this.configuredType = type;
		this.index = index;
		this.containerElementType = containerElementType;
		this.constraints = new HashSet<>();
		this.nestedContainerElementContexts = new HashMap<>();
	}

	@Override
	public PropertyHolderConstraintMappingContext propertyHolder(String property) {
		return typeContext.propertyHolder( property );
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		return typeContext.property( property, propertyType );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(Class<?> containerElementType) {
		return parentContainerElementTarget.containerElementType( containerElementType );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(Class<?> containerElementType, int index, int... nestedIndexes) {
		return parentContainerElementTarget.containerElementType( containerElementType, index, nestedIndexes );
	}

	@Override
	public CascadableContainerElementConstraintMappingContext containerElementType(String mapping) {
		return parentContainerElementTarget.containerElementType( mapping );
	}

	@Override
	public CascadableContainerElementConstraintMappingContext containerElementType(String mapping, int index, int... nestedIndexes) {
		return parentContainerElementTarget.containerElementType( mapping, index, nestedIndexes );
	}

	ContainerElementConstraintMappingContext nestedContainerElement(int[] nestedIndexes) {

		ContainerElementConstraintMappingContextImpl nestedContext = nestedContainerElementContexts.get( nestedIndexes[0] );
		if ( nestedContext == null ) {
			nestedContext = new ContainerElementConstraintMappingContextImpl(
					typeContext,
					parentContainerElementTarget,
					null,
					nestedIndexes[0],
					null
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
		constraints.add(
				new MetaConstraintBuilder(
						definition
				)
				// ConfiguredConstraint.forTypeArgument( definition, parentLocation, typeParameter, getContainerElementType() )
		);
		return this;
	}

	CascadingMetaDataBuilder getContainerElementCascadingMetaDataBuilder(ConstraintLocation parentLocation) {
		ConstraintLocation location = getCurrentConstraintLocation( parentLocation );
		return CascadingMetaDataBuilder.typeArgument(
				parentLocation.getTypeForValidatorResolution(),
				typeParameter,
				false,
				nestedContainerElementContexts.values()
						.stream()
						.map( context -> context.getContainerElementCascadingMetaDataBuilder( location ) )
						.collect( Collectors.toList() ),
				Collections.emptyMap()
		);
	}

	Set<MetaConstraint<?>> build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, ConstraintLocation parentLocation) {
		ConstraintLocation location = getCurrentConstraintLocation( parentLocation );
		return Stream.concat(
				constraints.stream()
						.map( c -> c.build( typeResolutionHelper, constraintHelper, valueExtractorManager, location ) ),
				nestedContainerElementContexts.values()
						.stream()
						.map( c -> c.build( constraintHelper, typeResolutionHelper, valueExtractorManager, location ) )
						.flatMap( Set::stream )
		)
				.collect( Collectors.toSet() );
	}

	private ConstraintLocation getCurrentConstraintLocation(ConstraintLocation parentLocation) {
		return ConstraintLocation.forTypeArgument( parentLocation, typeParameter, containerElementType );
	}
}
