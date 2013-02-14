/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.engine.path.MessageAndPath;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextImpl implements ConstraintValidatorContext {

	private static final Log log = LoggerFactory.make();

	private final List<MessageAndPath> messageAndPaths = new ArrayList<MessageAndPath>( 3 );
	private final PathImpl basePath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;

	public ConstraintValidatorContextImpl(PathImpl propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
		this.basePath = PathImpl.createCopy( propertyPath );
		this.constraintDescriptor = constraintDescriptor;
	}

	@Override
	public final void disableDefaultConstraintViolation() {
		defaultDisabled = true;
	}

	@Override
	public final String getDefaultConstraintMessageTemplate() {
		return (String) constraintDescriptor.getAttributes().get( "message" );
	}

	@Override
	public final ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return new ErrorBuilderImpl( messageTemplate, PathImpl.createCopy( basePath ) );
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( ConstraintValidatorContext.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedException( type );
	}

	public final ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public final List<MessageAndPath> getMessageAndPathList() {
		if ( defaultDisabled && messageAndPaths.size() == 0 ) {
			throw log.getAtLeastOneCustomMessageMustBeCreatedException();
		}

		List<MessageAndPath> returnedMessageAndPaths = new ArrayList<MessageAndPath>( messageAndPaths );
		if ( !defaultDisabled ) {
			returnedMessageAndPaths.add(
					new MessageAndPath( getDefaultConstraintMessageTemplate(), basePath )
			);
		}
		return returnedMessageAndPaths;
	}

	class ErrorBuilderImpl implements ConstraintViolationBuilder {
		private final String messageTemplate;
		private PathImpl propertyPath;

		ErrorBuilderImpl(String template, PathImpl path) {
			messageTemplate = template;
			propertyPath = path;
		}

		@Override
		public NodeBuilderDefinedContext addNode(String name) {
			// in case we are in a class level constraint and we want to add a node we drop the node representing the
			// class level (HF)
			if ( !propertyPath.isRootPath() && propertyPath.getLeafNode().getName() == null ) {
				propertyPath = propertyPath.getPathWithoutLeafNode();
			}
			propertyPath.addPropertyNode( name );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		@Override
		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}
	}

	class NodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderDefinedContext {
		private final String messageTemplate;
		private final PathImpl propertyPath;

		NodeBuilderImpl(String template, PathImpl path) {
			messageTemplate = template;
			propertyPath = path;
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		@Override
		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}
	}

	class InIterableNodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderCustomizableContext {
		private final String messageTemplate;
		private final PathImpl propertyPath;
		private final String leafNodeName;

		InIterableNodeBuilderImpl(String template, PathImpl path, String nodeName) {
			this.messageTemplate = template;
			this.propertyPath = path;
			this.leafNodeName = nodeName;
		}

		@Override
		public ConstraintViolationBuilder.NodeContextBuilder inIterable() {
			this.propertyPath.makeLeafNodeIterable();
			return new InIterablePropertiesBuilderImpl( messageTemplate, propertyPath, leafNodeName );
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addPropertyNode( leafNodeName );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		@Override
		public ConstraintValidatorContext addConstraintViolation() {
			propertyPath.addPropertyNode( leafNodeName );
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}
	}

	class InIterablePropertiesBuilderImpl implements ConstraintViolationBuilder.NodeContextBuilder {
		private final String messageTemplate;
		private final PathImpl propertyPath;
		private final String leafNodeName;

		InIterablePropertiesBuilderImpl(String template, PathImpl path, String nodeName) {
			this.messageTemplate = template;
			this.propertyPath = path;
			this.leafNodeName = nodeName;
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderDefinedContext atKey(Object key) {
			propertyPath.setLeafNodeMapKey( key );
			propertyPath.addPropertyNode( leafNodeName );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderDefinedContext atIndex(Integer index) {
			propertyPath.setLeafNodeIndex( index );
			propertyPath.addPropertyNode( leafNodeName );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addPropertyNode( leafNodeName );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		@Override
		public ConstraintValidatorContext addConstraintViolation() {
			propertyPath.addPropertyNode( leafNodeName );
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			//TODO HV-709
			throw new UnsupportedOperationException( "Not implemented yet" );
		}
	}
}
