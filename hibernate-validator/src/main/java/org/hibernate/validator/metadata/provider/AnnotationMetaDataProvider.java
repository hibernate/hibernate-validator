/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata.provider;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.ConstraintHelper;

/**
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProvider extends MetaDataProviderImplBase {

	private final Class<?> beanClass;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper, Class<?> beanClass) {

		super( constraintHelper );

		this.beanClass = beanClass;

		initDefaultGroupSequence();
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return new AnnotationIgnores();
	}

	/**
	 * Checks whether there is a default group sequence defined for this class.
	 * See HV-113.
	 */
	private void initDefaultGroupSequence() {
		GroupSequenceProvider groupSequenceProviderAnnotation = beanClass.getAnnotation( GroupSequenceProvider.class );
		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );

		if ( groupSequenceAnnotation != null && groupSequenceProviderAnnotation != null ) {
			throw new GroupDefinitionException(
					"GroupSequence and GroupSequenceProvider annotations cannot be used at the same time"
			);
		}

		List<Class<?>> defaultGroupSequence = groupSequenceAnnotation != null ? Arrays.asList( groupSequenceAnnotation.value() ) : null;
		Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider = groupSequenceProviderAnnotation != null ? groupSequenceProviderAnnotation
				.value() : null;

		if ( defaultGroupSequence == null && defaultGroupSequenceProvider == null ) {
			defaultGroupSequence = Arrays.<Class<?>>asList( beanClass );
		}

		configuredBeans.put(
				beanClass,
				createBeanConfiguration(
						beanClass,
						Collections.<BeanMetaConstraint<?>>emptySet(),
						Collections.<Member>emptySet(),
						defaultGroupSequence,
						defaultGroupSequenceProvider
				)
		);
	}
}
