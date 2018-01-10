/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.binding.ConstraintType;
import org.hibernate.validator.internal.xml.binding.ContainerElementTypeType;

/**
 * Builds the cascading and type argument constraints configuration from the {@link ContainerElementType} elements.
 *
 * @author Guillaume Smet
 */
class ContainerElementTypeConfigurationBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ConstraintLocation rootConstraintLocation;

	private final MetaConstraintBuilder metaConstraintBuilder;

	private final GroupConversionBuilder groupConversionBuilder;

	private final String defaultPackage;

	private final Set<ContainerElementTypePath> configuredPaths = new HashSet<>();

	ContainerElementTypeConfigurationBuilder(MetaConstraintBuilder metaConstraintBuilder, GroupConversionBuilder groupConversionBuilder,
			ConstraintLocation rootConstraintLocation, String defaultPackage) {
		this.metaConstraintBuilder = metaConstraintBuilder;
		this.groupConversionBuilder = groupConversionBuilder;
		this.rootConstraintLocation = rootConstraintLocation;
		this.defaultPackage = defaultPackage;
	}

	ContainerElementTypeConfiguration build(List<ContainerElementTypeType> xmlContainerElementTypes, Type enclosingType) {
		return add( ContainerElementTypePath.root(), xmlContainerElementTypes, rootConstraintLocation, enclosingType );
	}

	private ContainerElementTypeConfiguration add(ContainerElementTypePath parentConstraintElementTypePath, List<ContainerElementTypeType> xmlContainerElementTypes,
			ConstraintLocation parentConstraintLocation, Type enclosingType) {
		if ( xmlContainerElementTypes.isEmpty() ) {
			return new ContainerElementTypeConfiguration( Collections.emptySet(), Collections.emptyMap() );
		}

		// HV-1428 Container element support is disabled for arrays
		if ( TypeHelper.isArray( enclosingType ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( enclosingType );
		}

		if ( !( enclosingType instanceof ParameterizedType ) && !TypeHelper.isArray( enclosingType ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( enclosingType );
		}

		Set<MetaConstraint<?>> metaConstraints = new HashSet<>();
		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaDataBuilder =
				CollectionHelper.newHashMap( xmlContainerElementTypes.size() );

		boolean isArray = TypeHelper.isArray( enclosingType );
		TypeVariable<?>[] typeParameters = isArray ? new TypeVariable[0] : ReflectionHelper.getClassFromType( enclosingType ).getTypeParameters();

		for ( ContainerElementTypeType xmlContainerElementType : xmlContainerElementTypes ) {
			Integer typeArgumentIndex = getTypeArgumentIndex( xmlContainerElementType, typeParameters, isArray, enclosingType );

			ContainerElementTypePath constraintElementTypePath = ContainerElementTypePath.of( parentConstraintElementTypePath, typeArgumentIndex );
			boolean configuredBefore = !configuredPaths.add( constraintElementTypePath );
			if ( configuredBefore ) {
				throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaXmlMappingConfigurationException( rootConstraintLocation, constraintElementTypePath );
			}

			TypeVariable<?> typeParameter = getTypeParameter( typeParameters, typeArgumentIndex, isArray, enclosingType );
			Type containerElementType = getContainerElementType( enclosingType, typeArgumentIndex, isArray );
			ConstraintLocation containerElementTypeConstraintLocation = ConstraintLocation.forTypeArgument( parentConstraintLocation, typeParameter,
					containerElementType );

			for ( ConstraintType constraint : xmlContainerElementType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = metaConstraintBuilder.buildMetaConstraint(
						containerElementTypeConstraintLocation,
						constraint,
						java.lang.annotation.ElementType.TYPE_USE,
						defaultPackage,
						null
				);
				metaConstraints.add( metaConstraint );
			}

			ContainerElementTypeConfiguration nestedContainerElementTypeConfiguration = add( constraintElementTypePath, xmlContainerElementType.getContainerElementType(),
					containerElementTypeConstraintLocation, containerElementType );

			metaConstraints.addAll( nestedContainerElementTypeConfiguration.getMetaConstraints() );

			boolean isCascaded = xmlContainerElementType.getValid() != null;

			containerElementTypesCascadingMetaDataBuilder.put( typeParameter, new CascadingMetaDataBuilder( enclosingType, typeParameter, isCascaded,
					nestedContainerElementTypeConfiguration.getTypeParametersCascadingMetaData(),
					groupConversionBuilder.buildGroupConversionMap( xmlContainerElementType.getConvertGroup(), defaultPackage ) )
			);
		}

		return new ContainerElementTypeConfiguration( metaConstraints, containerElementTypesCascadingMetaDataBuilder );
	}

	private Integer getTypeArgumentIndex(ContainerElementTypeType xmlContainerElementType, TypeVariable<?>[] typeParameters, boolean isArray, Type enclosingType) {
		if ( isArray ) {
			return null;
		}

		Integer typeArgumentIndex = xmlContainerElementType.getTypeArgumentIndex();
		if ( typeArgumentIndex == null ) {
			if ( typeParameters.length > 1 ) {
				throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( enclosingType );
			}
			typeArgumentIndex = 0;
		}

		return typeArgumentIndex;
	}

	private TypeVariable<?> getTypeParameter(TypeVariable<?>[] typeParameters, Integer typeArgumentIndex, boolean isArray, Type enclosingType) {
		TypeVariable<?> typeParameter;
		if ( !isArray ) {
			if ( typeArgumentIndex > typeParameters.length - 1 ) {
				throw LOG.getInvalidTypeArgumentIndexException( enclosingType, typeArgumentIndex );
			}

			typeParameter = typeParameters[typeArgumentIndex];
		}
		else {
			typeParameter = new ArrayElement( enclosingType );
		}
		return typeParameter;
	}

	private Type getContainerElementType(Type enclosingType, Integer typeArgumentIndex, boolean isArray) {
		Type containerElementType;
		if ( !isArray ) {
			containerElementType = ( (ParameterizedType) enclosingType ).getActualTypeArguments()[typeArgumentIndex];
		}
		else {
			containerElementType = TypeHelper.getComponentType( enclosingType );
		}
		return containerElementType;
	}

	static class ContainerElementTypeConfiguration {

		private final Set<MetaConstraint<?>> metaConstraints;

		private final Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaDataBuilder;

		private ContainerElementTypeConfiguration(Set<MetaConstraint<?>> metaConstraints, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData) {
			this.metaConstraints = metaConstraints;
			this.containerElementTypesCascadingMetaDataBuilder = containerElementTypesCascadingMetaData;
		}

		public Set<MetaConstraint<?>> getMetaConstraints() {
			return metaConstraints;
		}

		public Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetaData() {
			return containerElementTypesCascadingMetaDataBuilder;
		}
	}
}
