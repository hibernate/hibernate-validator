/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * An aggregated view of the cascading validation metadata for containers. Note that it also includes the cascading
 * validation metadata defined on the root element via the {@link AnnotatedObject} pseudo type parameter.
 *
 * @author Guillaume Smet
 */
public class ContainerCascadingMetaData implements CascadingMetaData {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The enclosing type that defines this type parameter.
	 */
	private final Type enclosingType;

	/**
	 * The type parameter.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * The declared container class: it is the one used in the node of the property path.
	 */
	private final Class<?> declaredContainerClass;

	/**
	 * The declared type parameter: it is the one used in the node of the property path.
	 */
	private final TypeVariable<?> declaredTypeParameter;

	/**
	 * Index of the declared type parameter: it is the one used in the node of the property path.
	 * It is cached here for performance reasons.
	 */
	private final Integer declaredTypeParameterIndex;

	/**
	 * Possibly the cascading type parameters corresponding to this type parameter if it is a parameterized type.
	 */
	@Immutable
	private final List<ContainerCascadingMetaData> containerElementTypesCascadingMetaData;

	/**
	 * If this type parameter is marked for cascading.
	 */
	private final boolean cascading;

	/**
	 * The group conversions defined for this type parameter.
	 */
	private GroupConversionHelper groupConversionHelper;

	/**
	 * Whether any container element (it can be nested) is marked for cascaded validation.
	 */
	private final boolean hasContainerElementsMarkedForCascading;

	/**
	 * The set of value extractors which are type compliant and container element compliant with the element where
	 * the cascaded validation was declared. The final value extractor is chosen among these ones, based on the
	 * runtime type.
	 */
	private final Set<ValueExtractorDescriptor> valueExtractorCandidates;

	public static ContainerCascadingMetaData of(ValueExtractorManager valueExtractorManager, CascadingMetaDataBuilder cascadingMetaDataBuilder,
			Object context) {
		return new ContainerCascadingMetaData( valueExtractorManager, cascadingMetaDataBuilder );
	}

	private ContainerCascadingMetaData(ValueExtractorManager valueExtractorManager, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		this(
				valueExtractorManager,
				cascadingMetaDataBuilder.getEnclosingType(),
				cascadingMetaDataBuilder.getTypeParameter(),
				cascadingMetaDataBuilder.getDeclaredContainerClass(),
				cascadingMetaDataBuilder.getDeclaredTypeParameter(),
				cascadingMetaDataBuilder.getContainerElementTypesCascadingMetaData().entrySet().stream()
						.map( entry -> new ContainerCascadingMetaData( valueExtractorManager, entry.getValue() ) )
						.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) ),
				cascadingMetaDataBuilder.isCascading(),
				GroupConversionHelper.of( cascadingMetaDataBuilder.getGroupConversions() ),
				cascadingMetaDataBuilder.isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
		);
	}

	private ContainerCascadingMetaData(ValueExtractorManager valueExtractorManager, Type enclosingType, TypeVariable<?> typeParameter,
			Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter, List<ContainerCascadingMetaData> containerElementTypesCascadingMetaData,
			boolean cascading, GroupConversionHelper groupConversionHelper, boolean markedForCascadingOnContainerElements) {
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
		this.declaredContainerClass = declaredContainerClass;
		this.declaredTypeParameter = declaredTypeParameter;
		this.declaredTypeParameterIndex = TypeVariables.getTypeParameterIndex( declaredTypeParameter );
		this.containerElementTypesCascadingMetaData = containerElementTypesCascadingMetaData;
		this.cascading = cascading;
		this.groupConversionHelper = groupConversionHelper;
		this.hasContainerElementsMarkedForCascading = markedForCascadingOnContainerElements;

		if ( TypeVariables.isAnnotatedObject( this.typeParameter ) || !markedForCascadingOnContainerElements ) {
			this.valueExtractorCandidates = Collections.emptySet();
		}
		else {
			this.valueExtractorCandidates = CollectionHelper.toImmutableSet(
					valueExtractorManager.getResolver().getValueExtractorCandidatesForCascadedValidation( this.enclosingType, this.typeParameter )
			);

			if ( this.valueExtractorCandidates.size() == 0 ) {
				throw LOG.getNoValueExtractorFoundForTypeException( this.declaredContainerClass, this.declaredTypeParameter );
			}
		}
	}

	ContainerCascadingMetaData(Type enclosingType, List<ContainerCascadingMetaData> containerElementTypesCascadingMetaData,
			GroupConversionHelper groupConversionHelper, Set<ValueExtractorDescriptor> valueExtractorCandidates) {
		this.enclosingType = enclosingType;
		this.typeParameter = AnnotatedObject.INSTANCE;
		this.declaredContainerClass = null;
		this.declaredTypeParameter = null;
		this.declaredTypeParameterIndex = null;
		this.containerElementTypesCascadingMetaData = containerElementTypesCascadingMetaData;
		this.cascading = true;
		this.groupConversionHelper = groupConversionHelper;
		this.hasContainerElementsMarkedForCascading = true;
		this.valueExtractorCandidates = valueExtractorCandidates;
	}

	ContainerCascadingMetaData(Type enclosingType, TypeVariable<?> typeParameter, Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter,
			GroupConversionHelper groupConversionHelper) {
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
		this.declaredContainerClass = declaredContainerClass;
		this.declaredTypeParameter = declaredTypeParameter;
		this.declaredTypeParameterIndex = TypeVariables.getTypeParameterIndex( declaredTypeParameter );
		this.containerElementTypesCascadingMetaData = Collections.emptyList();
		this.cascading = true;
		this.groupConversionHelper = groupConversionHelper;
		this.hasContainerElementsMarkedForCascading = false;
		this.valueExtractorCandidates = Collections.emptySet();
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	public Type getEnclosingType() {
		return enclosingType;
	}

	public Class<?> getDeclaredContainerClass() {
		return declaredContainerClass;
	}

	public TypeVariable<?> getDeclaredTypeParameter() {
		return declaredTypeParameter;
	}

	public Integer getDeclaredTypeParameterIndex() {
		return declaredTypeParameterIndex;
	}

	@Override
	public boolean isCascading() {
		return cascading;
	}

	public boolean hasContainerElementsMarkedForCascading() {
		return hasContainerElementsMarkedForCascading;
	}

	@Override
	public boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements() {
		return cascading || hasContainerElementsMarkedForCascading;
	}

	public List<ContainerCascadingMetaData> getContainerElementTypesCascadingMetaData() {
		return containerElementTypesCascadingMetaData;
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConversionHelper.convertGroup( originalGroup );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CascadingMetaData> T as(Class<T> clazz) {
		if ( clazz.isAssignableFrom( getClass() ) ) {
			return (T) this;
		}

		throw LOG.getUnableToCastException( this, clazz );
	}

	@Override
	public CascadingMetaData addRuntimeContainerSupport(ValueExtractorManager valueExtractorManager, Class<?> valueClass) {
		return this;
	}

	public Set<ValueExtractorDescriptor> getValueExtractorCandidates() {
		return valueExtractorCandidates;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "enclosingType=" ).append( StringHelper.toShortString( enclosingType ) ).append( ", " );
		sb.append( "typeParameter=" ).append( typeParameter ).append( ", " );
		sb.append( "cascading=" ).append( cascading ).append( ", " );
		sb.append( "groupConversions=" ).append( groupConversionHelper ).append( ", " );
		sb.append( "containerElementTypesCascadingMetaData=" ).append( containerElementTypesCascadingMetaData );
		sb.append( "]" );
		return sb.toString();
	}
}
