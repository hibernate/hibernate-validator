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
	private final PathImpl propertyPath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;


	public ConstraintValidatorContextImpl(PathImpl propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
		this.propertyPath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
	}

	public final void disableDefaultConstraintViolation() {
		defaultDisabled = true;
	}

	public final String getDefaultConstraintMessageTemplate() {
		return (String) constraintDescriptor.getAttributes().get( "message" );
	}

	public final ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return new ErrorBuilderImpl( messageTemplate, propertyPath );
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
					new MessageAndPath( getDefaultConstraintMessageTemplate(), propertyPath )
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
			PathImpl path;
			if ( propertyPath.isRootPath() ) {
				path = PathImpl.createNewPath( name );
			}
			else {
				path = PathImpl.createShallowCopy( propertyPath );
				path.addNode( new NodeImpl( name ) );
			}
			return new NodeBuilderImpl( messageTemplate, path );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class NodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderDefinedContext {
		private String messageTemplate;
		private PathImpl propertyPath;

		NodeBuilderImpl(String template, PathImpl path) {
			messageTemplate = template;
			propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addNode( new NodeImpl( name ) );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class InIterableNodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderCustomizableContext {
		private String messageTemplate;
		private PathImpl propertyPath;

		InIterableNodeBuilderImpl(String template, PathImpl path) {
			this.messageTemplate = template;
			this.propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeContextBuilder inIterable() {
			this.propertyPath.getLeafNode().setInIterable( true );
			return new InIterablePropertiesBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addNode( new NodeImpl( name ) );
			return this;
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class InIterablePropertiesBuilderImpl implements ConstraintViolationBuilder.NodeContextBuilder {
		private String messageTemplate;
		private PathImpl propertyPath;

		InIterablePropertiesBuilderImpl(String template, PathImpl path) {
			this.messageTemplate = template;
			this.propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atKey(Object key) {
			propertyPath.getLeafNode().setKey( key );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atIndex(Integer index) {
			propertyPath.getLeafNode().setIndex( index );
			return new NodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath.addNode( new NodeImpl( name ) );
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}
}
