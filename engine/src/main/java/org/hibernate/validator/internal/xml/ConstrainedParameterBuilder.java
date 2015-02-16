/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint parameters.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedParameterBuilder {

	private final GroupConversionBuilder groupConversionBuilder;
	private final ParameterNameProvider parameterNameProvider;
	private final MetaConstraintBuilder metaConstraintBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	ConstrainedParameterBuilder(MetaConstraintBuilder metaConstraintBuilder,
			ParameterNameProvider parameterNameProvider, GroupConversionBuilder groupConversionBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.metaConstraintBuilder = metaConstraintBuilder;
		this.parameterNameProvider = parameterNameProvider;
		this.groupConversionBuilder = groupConversionBuilder;
		this.annotationProcessingOptions = annotationProcessingOptions;
	}

	List<ConstrainedParameter> buildConstrainedParameters(List<ParameterType> parameterList,
																		ExecutableElement executableElement,
																		String defaultPackage) {
		List<ConstrainedParameter> constrainedParameters = newArrayList();
		int i = 0;
		List<String> parameterNames = executableElement.getParameterNames( parameterNameProvider );
		for ( ParameterType parameterType : parameterList ) {
			ConstraintLocation constraintLocation = ConstraintLocation.forParameter( executableElement, i );
			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : parameterType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = metaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						ElementType.PARAMETER,
						defaultPackage,
						null
				);
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = groupConversionBuilder.buildGroupConversionMap(
					parameterType.getConvertGroup(),
					defaultPackage
			);

			// ignore annotations
			if ( parameterType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnParameter(
						executableElement.getMember(),
						i,
						parameterType.getIgnoreAnnotations()
				);
			}

			// TODO HV-919 Support specification of type parameter constraints via XML and API
			ConstrainedParameter constrainedParameter = new ConstrainedParameter(
					ConfigurationSource.XML,
					constraintLocation,
					ReflectionHelper.typeOf( executableElement, i ),
					i,
					parameterNames.get( i ),
					metaConstraints,
					Collections.<MetaConstraint<?>>emptySet(),
					groupConversions,
					parameterType.getValid() != null,
					UnwrapMode.AUTOMATIC
			);
			constrainedParameters.add( constrainedParameter );
			i++;
		}

		return constrainedParameters;
	}
}


