/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builds the cascading and type argument constraints configuration from the {@code <container-element-type>} elements.
 *
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ContainerElementTypeStaxBuilder extends AbstractStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String CONTAINER_ELEMENT_TYPE_QNAME_LOCAL_PART = "container-element-type";
	private static final QName TYPE_ARGUMENT_INDEX_QNAME = new QName( "type-argument-index" );

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintCreationContext constraintCreationContext;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;

	private Integer typeArgumentIndex;
	private final ValidStaxBuilder validStaxBuilder;
	private final List<ConstraintTypeStaxBuilder> constraintTypeStaxBuilders;
	private final GroupConversionStaxBuilder groupConversionBuilder;
	private final List<ContainerElementTypeStaxBuilder> containerElementTypeConfigurationStaxBuilders;

	ContainerElementTypeStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.groupConversionBuilder = new GroupConversionStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.validStaxBuilder = new ValidStaxBuilder();
		this.constraintTypeStaxBuilders = new ArrayList<>();
		this.containerElementTypeConfigurationStaxBuilders = new ArrayList<>();

	}

	@Override
	protected String getAcceptableQName() {
		return CONTAINER_ELEMENT_TYPE_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		Optional<String> typeArgumentIndex = readAttribute( xmlEvent.asStartElement(), TYPE_ARGUMENT_INDEX_QNAME );
		if ( typeArgumentIndex.isPresent() ) {
			this.typeArgumentIndex = Integer.parseInt( typeArgumentIndex.get() );
		}
		ConstraintTypeStaxBuilder constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
		ContainerElementTypeStaxBuilder containerElementTypeConfigurationStaxBuilder = getNewContainerElementTypeConfigurationStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			validStaxBuilder.process( xmlEventReader, xmlEvent );
			groupConversionBuilder.process( xmlEventReader, xmlEvent );
			if ( constraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintTypeStaxBuilders.add( constraintTypeStaxBuilder );
				constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
			}
			if ( containerElementTypeConfigurationStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				containerElementTypeConfigurationStaxBuilders.add( containerElementTypeConfigurationStaxBuilder );
				containerElementTypeConfigurationStaxBuilder = getNewContainerElementTypeConfigurationStaxBuilder();
			}
		}
	}

	private ConstraintTypeStaxBuilder getNewConstraintTypeStaxBuilder() {
		return new ConstraintTypeStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder );
	}

	private ContainerElementTypeStaxBuilder getNewContainerElementTypeConfigurationStaxBuilder() {
		return new ContainerElementTypeStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder );
	}

	public ContainerElementTypeConfiguration build(Set<ContainerElementTypePath> configuredPaths,
			ContainerElementTypePath parentConstraintElementTypePath,
			ConstraintLocation parentConstraintLocation, Type enclosingType) {
		// HV-1428 Container element support is disabled for arrays
		if ( TypeHelper.isArray( enclosingType ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( enclosingType );
		}

		if ( !( enclosingType instanceof ParameterizedType ) && !TypeHelper.isArray( enclosingType ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( enclosingType );
		}

		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaDataBuilder =
				CollectionHelper.newHashMap( containerElementTypeConfigurationStaxBuilders.size() );

		boolean isArray = TypeHelper.isArray( enclosingType );
		TypeVariable<?>[] typeParameters = isArray ? new TypeVariable[0] : ReflectionHelper.getClassFromType( enclosingType ).getTypeParameters();

		Integer typeArgumentIndex = getTypeArgumentIndex( typeParameters, isArray, enclosingType );

		ContainerElementTypePath constraintElementTypePath = ContainerElementTypePath.of( parentConstraintElementTypePath, typeArgumentIndex );
		boolean configuredBefore = !configuredPaths.add( constraintElementTypePath );
		if ( configuredBefore ) {
			throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaXmlMappingConfigurationException( parentConstraintLocation, constraintElementTypePath );
		}

		TypeVariable<?> typeParameter = getTypeParameter( typeParameters, typeArgumentIndex, isArray, enclosingType );
		Type containerElementType = getContainerElementType( enclosingType, typeArgumentIndex, isArray );
		ConstraintLocation containerElementTypeConstraintLocation = ConstraintLocation.forTypeArgument( parentConstraintLocation, typeParameter,
				containerElementType
		);

		ContainerElementTypeConfiguration nestedContainerElementTypeConfiguration = containerElementTypeConfigurationStaxBuilders.stream()
				.map( nested -> nested.build( configuredPaths, constraintElementTypePath, containerElementTypeConstraintLocation, containerElementType ) )
				.reduce( ContainerElementTypeConfiguration.EMPTY_CONFIGURATION, ContainerElementTypeConfiguration::merge );

		boolean isCascaded = validStaxBuilder.build();

		containerElementTypesCascadingMetaDataBuilder.put( typeParameter, new CascadingMetaDataBuilder( enclosingType, typeParameter, isCascaded,
						nestedContainerElementTypeConfiguration.getTypeParametersCascadingMetaData(),
						groupConversionBuilder.build()
				)
		);

		return new ContainerElementTypeConfiguration(
				Stream.concat(
						constraintTypeStaxBuilders.stream()
								.map(
										builder -> builder.build(
												containerElementTypeConstraintLocation,
												ConstraintLocation.ConstraintLocationKind.TYPE_USE,
												null
										)
								),
						nestedContainerElementTypeConfiguration.getMetaConstraints().stream()
				).collect( Collectors.toSet() ),
				containerElementTypesCascadingMetaDataBuilder
		);
	}

	private Integer getTypeArgumentIndex(TypeVariable<?>[] typeParameters, boolean isArray, Type enclosingType) {
		if ( isArray ) {
			return null;
		}

		if ( typeArgumentIndex == null ) {
			if ( typeParameters.length > 1 ) {
				throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( enclosingType );
			}
			return 0;
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

	public Integer getTypeArgumentIndex() {
		return null;
	}
}
