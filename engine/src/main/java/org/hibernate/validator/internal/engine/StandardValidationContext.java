/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine;

import java.util.Iterator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * A {@link ValidationContext} implementation which creates and manages violations of type {@link ConstraintViolation}.
 *
 * @param <T> The type of the root bean for which this context is created.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class StandardValidationContext<T> extends ValidationContext<T, ConstraintViolation<T>> {

	protected StandardValidationContext(BeanMetaDataManager beanMetaDataManager,
										Class<T> rootBeanClass,
										T rootBean,
										MessageInterpolator messageInterpolator,
										ConstraintValidatorFactory constraintValidatorFactory,
										TraversableResolver traversableResolver,
										boolean failFast) {

		super(
				beanMetaDataManager,
				rootBeanClass,
				rootBean,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	@Override
	public <U, V> ConstraintViolation<T> createConstraintViolation(
			ValueContext<U, V> localContext, MessageAndPath messageAndPath,
			ConstraintDescriptor<?> descriptor) {

		String messageTemplate = messageAndPath.getMessage();
		String interpolatedMessage = messageInterpolator.interpolate(
				messageTemplate,
				new MessageInterpolatorContext( descriptor, localContext.getCurrentValidatedValue() )
		);

		attachElementDescriptorToPathNodes( messageAndPath );

		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				getRootBeanClass(),
				getRootBean(),
				localContext.getCurrentBean(),
				localContext.getCurrentValidatedValue(),
				messageAndPath.getPath(),
				descriptor,
				localContext.getElementType()
		);
	}

	private void attachElementDescriptorToPathNodes(MessageAndPath messageAndPath) {
		Class<?> currentClass = getRootBeanClass();

		Iterator<Path.Node> nodeIterator = messageAndPath.getPath().iterator();
		while ( nodeIterator.hasNext() ) {
			BeanMetaData beanMetaData = getBeanMetaDataManager().getBeanMetaData( currentClass );
			NodeImpl node = (NodeImpl) nodeIterator.next();
			String name = node.getName();
			ElementDescriptor elementDescriptor;
			if ( name == null ) {
				// this is a class level constraint
				elementDescriptor = beanMetaData.getBeanDescriptor();
			}
			else {
				elementDescriptor = beanMetaData.getBeanDescriptor().getConstraintsForProperty( name );
				if ( elementDescriptor instanceof PropertyDescriptor && ( (PropertyDescriptor) elementDescriptor ).isCascaded() ) {
					beanMetaData = getBeanMetaDataManager().getBeanMetaData( elementDescriptor.getElementClass() );
					elementDescriptor = beanMetaData.getBeanDescriptor();
				}
			}

			// TODO -  propertyDescriptor.getElementClass() returns eg just Collection instead of the element type (HF)
			if ( elementDescriptor != null ) {
				node.setElementDescriptor( elementDescriptor );
				currentClass = elementDescriptor.getElementClass();
			}
		}
	}
}
