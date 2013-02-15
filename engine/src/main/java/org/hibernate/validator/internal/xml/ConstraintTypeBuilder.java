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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint types.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintTypeBuilder {

	private ConstraintTypeBuilder() {
	}

	public static ConstrainedType buildConstrainedType(ClassType classType,
													   Class<?> beanClass,
													   String defaultPackage,
													   ConstraintHelper constraintHelper,
													   AnnotationProcessingOptionsImpl annotationProcessingOptions,
													   Map<Class<?>, List<Class<?>>> defaultSequences) {
		if ( classType == null ) {
			return null;
		}

		// group sequence
		List<Class<?>> groupSequence = createGroupSequence( classType.getGroupSequence(), defaultPackage );
		if ( !groupSequence.isEmpty() ) {
			defaultSequences.put( beanClass, groupSequence );
		}

		// constraints
		BeanConstraintLocation constraintLocation = new BeanConstraintLocation( beanClass );
		Set<MetaConstraint<?>> metaConstraints = newHashSet();
		for ( ConstraintType constraint : classType.getConstraint() ) {
			ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintDescriptorBuilder.buildConstraintDescriptor(
					constraint,
					java.lang.annotation.ElementType.TYPE,
					defaultPackage,
					constraintHelper
			);
			@SuppressWarnings("unchecked")
			MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
			metaConstraints.add( metaConstraint );
		}

		// ignore annotation
		if ( classType.getIgnoreAnnotations() != null ) {
			annotationProcessingOptions.ignoreClassLevelConstraintAnnotations(
					beanClass,
					classType.getIgnoreAnnotations()
			);
		}

		ConstrainedType constrainedType = new ConstrainedType(
				ConfigurationSource.XML,
				constraintLocation,
				metaConstraints
		);

		return constrainedType;
	}

	private static List<Class<?>> createGroupSequence(GroupSequenceType groupSequenceType, String defaultPackage) {
		List<Class<?>> groupSequence = newArrayList();
		if ( groupSequenceType != null ) {
			for ( String groupName : groupSequenceType.getValue() ) {
				Class<?> group = ReflectionHelper.loadClass( groupName, defaultPackage );
				groupSequence.add( group );
			}
		}
		return groupSequence;
	}
}


