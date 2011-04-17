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
package org.hibernate.validator.engine;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextImpl implements ConstraintValidatorContext {

	private final List<MessageAndPath> messageAndPaths = new ArrayList<MessageAndPath>( 3 );
	private final PathImpl basePath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;


	public ConstraintValidatorContextImpl(PathImpl propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
		this.basePath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
	}

	public final void disableDefaultConstraintViolation() {
		defaultDisabled = true;
	}

	public final String getDefaultConstraintMessageTemplate() {
		return (String) constraintDescriptor.getAttributes().get( "message" );
	}

	public final ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return new ErrorBuilderImpl( messageTemplate, PathImpl.createCopy( basePath ) );
	}

	public final ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public final List<MessageAndPath> getMessageAndPathList() {
		if ( defaultDisabled && messageAndPaths.size() == 0 ) {
			throw new ValidationException(
					"At least one custom message must be created if the default error message gets disabled."
			);
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
		private String messageTemplate;
		private PathImpl propertyPath;

		ErrorBuilderImpl(String template, PathImpl path) {
			messageTemplate = template;
			propertyPath = path;
		}

		public NodeBuilderDefinedContext addNode(String name) {
			propertyPath.addNode( name );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class NodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderDefinedContext {
		private final String messageTemplate;
		private final PathImpl propertyPath;

		NodeBuilderImpl(String template, PathImpl path) {
			messageTemplate = template;
			propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
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

		public ConstraintViolationBuilder.NodeContextBuilder inIterable() {
			this.propertyPath.makeLeafNodeIterable();
			return new InIterablePropertiesBuilderImpl( messageTemplate, propertyPath, leafNodeName );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addNode( leafNodeName );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			propertyPath.addNode( leafNodeName );
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
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

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atKey(Object key) {
			propertyPath.setLeafNodeMapKey( key );
			propertyPath.addNode( leafNodeName );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atIndex(Integer index) {
			propertyPath.setLeafNodeIndex( index );
			propertyPath.addNode( leafNodeName );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addNode( leafNodeName );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath, name );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			propertyPath.addNode( leafNodeName );
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}
}
