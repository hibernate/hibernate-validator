/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.xml.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;
import org.hibernate.validator.internal.xml.binding.ConstraintType;
import org.hibernate.validator.internal.xml.binding.FieldType;

/**
 * Builder for constraint fields.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
class ConstrainedFieldBuilder {
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final GroupConversionBuilder groupConversionBuilder;
	private final MetaConstraintBuilder metaConstraintBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	ConstrainedFieldBuilder(MetaConstraintBuilder metaConstraintBuilder, GroupConversionBuilder groupConversionBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.metaConstraintBuilder = metaConstraintBuilder;
		this.groupConversionBuilder = groupConversionBuilder;
		this.annotationProcessingOptions = annotationProcessingOptions;
	}

	Set<ConstrainedField> buildConstrainedFields(List<FieldType> fields,
															   Class<?> beanClass,
															   String defaultPackage) {
		Set<ConstrainedField> constrainedFields = new HashSet<>();
		List<String> alreadyProcessedFieldNames = new ArrayList<>();
		for ( FieldType fieldType : fields ) {
			Field field = findField( beanClass, fieldType.getName(), alreadyProcessedFieldNames );
			ConstraintLocation constraintLocation = ConstraintLocation.forField( field );
			Set<MetaConstraint<?>> metaConstraints = new HashSet<>();

			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = metaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						java.lang.annotation.ElementType.FIELD,
						defaultPackage,
						null
				);
				metaConstraints.add( metaConstraint );
			}

			ContainerElementTypeConfigurationBuilder containerElementTypeConfigurationBuilder = new ContainerElementTypeConfigurationBuilder(
					metaConstraintBuilder, groupConversionBuilder, constraintLocation, defaultPackage );
			ContainerElementTypeConfiguration containerElementTypeConfiguration = containerElementTypeConfigurationBuilder
					.build( fieldType.getContainerElementType(), ReflectionHelper.typeOf( field ) );

			ConstrainedField constrainedField = new ConstrainedField(
					ConfigurationSource.XML,
					field,
					metaConstraints,
					containerElementTypeConfiguration.getMetaConstraints(),
					getCascadingMetaDataForField( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), field, fieldType, defaultPackage )
			);
			constrainedFields.add( constrainedField );

			// ignore annotations
			if ( fieldType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						field,
						fieldType.getIgnoreAnnotations()
				);
			}
		}

		return constrainedFields;
	}

	private CascadingMetaDataBuilder getCascadingMetaDataForField(Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Field field,
			FieldType fieldType, String defaultPackage) {
		Type type = ReflectionHelper.typeOf( field );
		boolean isCascaded = fieldType.getValid() != null;
		Map<Class<?>, Class<?>> groupConversions = groupConversionBuilder.buildGroupConversionMap(
				fieldType.getConvertGroup(),
				defaultPackage
		);

		return CascadingMetaDataBuilder.annotatedObject( type, isCascaded, containerElementTypesCascadingMetaData, groupConversions );
	}

	private static Field findField(Class<?> beanClass, String fieldName, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( fieldName ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( fieldName, beanClass );
		}
		else {
			alreadyProcessedFieldNames.add( fieldName );
		}

		final Field field = run( GetDeclaredField.action( beanClass, fieldName ) );
		if ( field == null ) {
			throw LOG.getBeanDoesNotContainTheFieldException( beanClass, fieldName );
		}
		return field;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
