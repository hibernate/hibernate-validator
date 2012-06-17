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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.BeanMetaDataLocator;
import org.hibernate.validator.internal.engine.path.MessageAndPath;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ConstraintMetaData.ConstraintMetaDataKind;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * A {@link ValidationContext} implementation which creates and manages
 * method validation violations.
 *
 * @param <T> The type of the root bean for which this context is created.
 *
 * @author Gunnar Morling
 */
public class MethodValidationContext<T> extends ValidationContext<T, ConstraintViolation<T>> {

	/**
	 * The method of the current validation call.
	 */
	private final ExecutableElement method;

	/**
	 * The index of the parameter to validate if this context is used for validation of a single parameter, {@code null} otherwise.
	 */
	private final Object[] parameterValues;

	protected MethodValidationContext(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			Class<T> rootBeanClass,
			T rootBean,
			ExecutableElement method,
			Object[] parameterValues,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {

		super(
				beanMetaDataManager,
				constraintValidatorManager,
				rootBeanClass,
				rootBean,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);

		this.method = method;
		this.parameterValues = parameterValues;
	}

	public ExecutableElement getExecutable() {
		return method;
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

		Path path = createPathWithElementDescriptors( messageAndPath.getPath(), localContext );

		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				getRootBeanClass(),
				getRootBean(),
				localContext.getCurrentBean(),
				localContext.getCurrentValidatedValue(),
				path,
				descriptor,
				localContext.getElementType()
		);
	}

	private Path createPathWithElementDescriptors(Path path, ValueContext<?, ?> localContext) {
		List<ElementDescriptor> elementDescriptors = new ArrayList<ElementDescriptor>();

		// first node in method level validation is the method and its descriptor
		ExecutableDescriptorImpl executableDescriptor = getMethodDescriptor();
		elementDescriptors.add( getMethodDescriptor() );

		Object value;
		if ( isReturnValueValidation( localContext ) ) {
			// add the return value descriptor
			elementDescriptors.add( executableDescriptor.getReturnValueDescriptor() );
			value = localContext.getCurrentBean();

			if ( value != null && ReflectionHelper.isIterable( value.getClass() ) ) {
				value = ReflectionHelper.getIndexedValue( value, getIterableIndex( path ) );
			}
		}
		else {
			// add the parameter descriptor
			Integer parameterIndex = localContext.getParameterIndex();
			ParameterDescriptor parameterDescriptor = executableDescriptor.getParameterDescriptors()
					.get( parameterIndex );
			elementDescriptors.add( parameterDescriptor );

			value = parameterValues[localContext.getParameterIndex()];

			if ( value != null && ReflectionHelper.isIterable( value.getClass() ) ) {
				value = ReflectionHelper.getIndexedValue( value, getIterableIndex( path ) );
			}
		}

		// if the value is not null we have a cascaded validation and the rest of the path is property path as in
		// bean validation
		if ( value != null ) {
			addDescriptorsForPropertyPart( path, elementDescriptors, value );
		}

		return PathImpl.createCopyWithElementDescriptorsAttached(
				(PathImpl) path,
				elementDescriptors
		);
	}

	private void addDescriptorsForPropertyPart(Path path, List<ElementDescriptor> elementDescriptors, Object value) {
		BeanMetaDataLocator traverser = BeanMetaDataLocator.createBeanMetaDataLocatorForBeanValidation(
				value,
				value.getClass(),
				getBeanMetaDataManager()
		);

		// need to sync up the two iterators
		Iterator<BeanMetaData<?>> beanMetaDataIterator = traverser.beanMetaDataIterator(
				advanceIteratorToCascadedNode(
						path
				)
		);
		Iterator<Path.Node> nodeIterator = advanceIteratorToCascadedNode( path );
		while ( nodeIterator.hasNext() ) {
			Path.Node node = nodeIterator.next();
			BeanMetaData<?> beanMetaData = beanMetaDataIterator.next();
			if ( isClassLevelConstraintNode( node.getName() ) ) {
				BeanDescriptor beanDescriptor = beanMetaData.getBeanDescriptor();
				elementDescriptors.add( beanDescriptor );
			}
			else {
				PropertyDescriptor propertyDescriptor = beanMetaData.getBeanDescriptor()
						.getConstraintsForProperty( node.getName() );

				elementDescriptors.add( propertyDescriptor );
			}
		}
	}

	private Iterator<Path.Node> advanceIteratorToCascadedNode(Path path) {
		Iterator<Path.Node> nodeIterator = path.iterator();
		nodeIterator.next();
		nodeIterator.next();
		return nodeIterator;
	}

	private int getIterableIndex(Path path) {
		// assuming we have an iterable return value or parameter we get the index of the validated instance
		Iterator<Path.Node> nodeIterator = path.iterator();
		nodeIterator.next(); // method node
		nodeIterator.next(); // return value or parameter node
		Path.Node node = nodeIterator.next(); // this node contains the index
		return node.getIndex();
	}

	private boolean isReturnValueValidation(ValueContext<?, ?> localContext) {
		return localContext.getParameterIndex() == null;
	}

	private ExecutableDescriptorImpl getMethodDescriptor() {
		BeanMetaData<?> rootMetaData = getBeanMetaDataManager().getBeanMetaData( getRootBeanClass() );
		ExecutableMetaData methodMetaData = rootMetaData.getMetaDataFor( method );
		BeanDescriptor beanDescriptor = rootMetaData.getBeanDescriptor();

		//HV-571: Avoid these casts
		if ( methodMetaData.getKind() == ConstraintMetaDataKind.METHOD ) {
			return (ExecutableDescriptorImpl) beanDescriptor.getConstraintsForMethod(
					method.getMember().getName(),
					methodMetaData.getParameterTypes()
			);
		}
		else {
			return (ExecutableDescriptorImpl) beanDescriptor.getConstraintsForConstructor(
					methodMetaData.getParameterTypes()
			);
		}
	}

	private boolean isClassLevelConstraintNode(String name) {
		return name == null;
	}
}
