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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint getters.
 *
 * @author Hardy Ferentschik
 */
public class ConstrainedGetterBuilder {
	private static final Log log = LoggerFactory.make();

	private ConstrainedGetterBuilder() {
	}

	public static Set<ConstrainedExecutable> buildConstrainedGetters(List<GetterType> getterList,
																	 Class<?> beanClass,
																	 String defaultPackage,
																	 ConstraintHelper constraintHelper,
																	 AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		List<String> alreadyProcessedGetterNames = newArrayList();
		for ( GetterType getterType : getterList ) {
			String getterName = getterType.getName();
			Method getter = findGetter( beanClass, getterName, alreadyProcessedGetterNames );
			ExecutableConstraintLocation constraintLocation = new ExecutableConstraintLocation( getter );

			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : getterType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = MetaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						java.lang.annotation.ElementType.METHOD,
						defaultPackage,
						constraintHelper
				);
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = GroupConversionBuilder.buildGroupConversionMap(
					getterType.getConvertGroup(),
					defaultPackage
			);

			ConstrainedExecutable constrainedGetter = new ConstrainedExecutable(
					ConfigurationSource.XML,
					constraintLocation,
					Collections.<ConstrainedParameter>emptyList(),
					Collections.<MetaConstraint<?>>emptySet(),
					metaConstraints,
					groupConversions,
					getterType.getValid() != null
			);
			constrainedExecutables.add( constrainedGetter );

			// ignore annotations
			if ( getterType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						getter,
						getterType.getIgnoreAnnotations()
				);
			}
		}

		return constrainedExecutables;
	}

	private static Method findGetter(Class<?> beanClass, String getterName, List<String> alreadyProcessedGetterNames) {
		if ( alreadyProcessedGetterNames.contains( getterName ) ) {
			throw log.getIsDefinedTwiceInMappingXmlForBeanException( getterName, beanClass.getName() );
		}
		else {
			alreadyProcessedGetterNames.add( getterName );
		}

		final Method method = ReflectionHelper.getMethodFromPropertyName( beanClass, getterName );
		if ( method == null ) {
			throw log.getBeanDoesNotContainThePropertyException( beanClass.getName(), getterName );
		}

		return method;
	}
}


