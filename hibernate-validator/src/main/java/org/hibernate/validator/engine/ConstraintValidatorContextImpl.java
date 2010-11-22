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
	private final String propertyPath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;

	public ConstraintValidatorContextImpl(String propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
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
		private String propertyPath;

		ErrorBuilderImpl(String template, String path) {
			messageTemplate = template;
			propertyPath = path;
		}

		public NodeBuilderDefinedContext addNode(String name) {
			String path = propertyPath;
			if ( !PathImpl.ROOT_PATH.equals( propertyPath ) ) {
				path += PathImpl.PROPERTY_PATH_SEPARATOR;
			}
			path += name;
			return new NodeBuilderImpl( messageTemplate, path );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class NodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderDefinedContext {
		private String messageTemplate;
		private String propertyPath;

		NodeBuilderImpl(String template, String path) {
			messageTemplate = template;
			propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			if ( name != null ) {
				propertyPath += PathImpl.PROPERTY_PATH_SEPARATOR + name;
			}
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class InIterableNodeBuilderImpl implements ConstraintViolationBuilder.NodeBuilderCustomizableContext {
		private String messageTemplate;
		private String propertyPath;

		InIterableNodeBuilderImpl(String template, String path) {
			this.messageTemplate = template;
			this.propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeContextBuilder inIterable() {
			int lastPropertyIndex = propertyPath.lastIndexOf( PathImpl.PROPERTY_PATH_SEPARATOR );
			StringBuilder builder = new StringBuilder();

			if ( lastPropertyIndex != -1 ) {
				builder = new StringBuilder();
				builder.append( propertyPath.substring( 0, lastPropertyIndex ) );
				builder.append( PathImpl.INDEX_OPEN );
				builder.append( PathImpl.INDEX_CLOSE );
				builder.append( PathImpl.PROPERTY_PATH_SEPARATOR );
				builder.append( propertyPath.substring( lastPropertyIndex + 1 ) );
			}
			else {
				builder.append( propertyPath );
				builder.append( PathImpl.INDEX_OPEN );
				builder.append( PathImpl.INDEX_CLOSE );
			}

			return new InIterablePropertiesBuilderImpl( messageTemplate, builder.toString() );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath += PathImpl.PROPERTY_PATH_SEPARATOR + name;
			return this;
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}
	}

	class InIterablePropertiesBuilderImpl implements ConstraintViolationBuilder.NodeContextBuilder {
		private String messageTemplate;
		private String propertyPath;

		InIterablePropertiesBuilderImpl(String template, String path) {
			this.messageTemplate = template;
			this.propertyPath = path;
		}

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atKey(Object key) {
			StringBuilder builder = addKeyOrIndex( key );
			return new NodeBuilderImpl( messageTemplate, builder.toString() );
		}

		public ConstraintViolationBuilder.NodeBuilderDefinedContext atIndex(Integer index) {
			StringBuilder builder = addKeyOrIndex( index );
			return new NodeBuilderImpl( messageTemplate, builder.toString() );
		}

		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			propertyPath += PathImpl.PROPERTY_PATH_SEPARATOR + name;
			return new InIterableNodeBuilderImpl( messageTemplate, propertyPath );
		}

		public ConstraintValidatorContext addConstraintViolation() {
			messageAndPaths.add( new MessageAndPath( messageTemplate, propertyPath ) );
			return ConstraintValidatorContextImpl.this;
		}

		private StringBuilder addKeyOrIndex(Object key) {
			int index = propertyPath.lastIndexOf( PathImpl.INDEX_OPEN );
			StringBuilder builder = new StringBuilder();
			builder.append( propertyPath.substring( 0, index ) );
			builder.append( PathImpl.INDEX_OPEN );
			builder.append( key );
			builder.append( PathImpl.INDEX_CLOSE );
			if ( propertyPath.lastIndexOf( PathImpl.PROPERTY_PATH_SEPARATOR ) != -1 ) {
				builder.append( PathImpl.PROPERTY_PATH_SEPARATOR );
				builder.append( propertyPath.substring( index + 3 ) );
			}
			return builder;
		}
	}
}
