/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.xml;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint fields.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedFieldBuilder {
	private static final Log log = LoggerFactory.make();

	private ConstrainedFieldBuilder() {
	}

	static Set<ConstrainedField> buildConstrainedFields(List<FieldType> fields,
															   Class<?> beanClass,
															   String defaultPackage,
															   ConstraintHelper constraintHelper,
															   AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		Set<ConstrainedField> constrainedFields = newHashSet();
		List<String> alreadyProcessedFieldNames = newArrayList();
		for ( FieldType fieldType : fields ) {
			Field field = findField( beanClass, fieldType.getName(), alreadyProcessedFieldNames );
			ConstraintLocation constraintLocation = ConstraintLocation.forProperty( field );
			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = MetaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						java.lang.annotation.ElementType.FIELD,
						defaultPackage,
						constraintHelper,
						null
				);
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = GroupConversionBuilder.buildGroupConversionMap(
					fieldType.getConvertGroup(),
					defaultPackage
			);

			ConstrainedField constrainedField = new ConstrainedField(
					ConfigurationSource.XML,
					constraintLocation,
					metaConstraints,
					groupConversions,
					fieldType.getValid() != null,
					false
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

	private static Field findField(Class<?> beanClass, String fieldName, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( fieldName ) ) {
			throw log.getIsDefinedTwiceInMappingXmlForBeanException( fieldName, beanClass.getName() );
		}
		else {
			alreadyProcessedFieldNames.add( fieldName );
		}

		final Field field = run( GetDeclaredField.action( beanClass, fieldName ) );
		if ( field == null ) {
			throw log.getBeanDoesNotContainTheFieldException( beanClass.getName(), fieldName );
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
