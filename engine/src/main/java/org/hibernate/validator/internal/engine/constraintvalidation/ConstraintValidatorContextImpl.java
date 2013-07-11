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
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeContextBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeContextBuilder;
import javax.validation.ElementKind;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ConstraintValidatorContextImpl implements HibernateConstraintValidatorContext {

	private static final Log log = LoggerFactory.make();

	private final Map<String, Object> expressionVariables = newHashMap();
	private final List<String> methodParameterNames;
	private final List<ConstraintViolationCreationContext> constraintViolationCreationContexts = newArrayList( 3 );
	private final PathImpl basePath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;

	public ConstraintValidatorContextImpl(List<String> methodParameterNames, PathImpl propertyPath,
			ConstraintDescriptor<?> constraintDescriptor) {
		this.methodParameterNames = methodParameterNames;
		this.basePath = propertyPath;
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
		return new ConstraintViolationBuilderImpl(
				methodParameterNames,
				messageTemplate,
				PathImpl.createCopy( basePath )
		);
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateConstraintValidatorContext.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateConstraintValidatorContext addExpressionVariable(String name, Object value) {
		Contracts.assertNotNull( name, "null is not a valid value" );
		this.expressionVariables.put( name, value );
		return this;
	}

	public final ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public final List<ConstraintViolationCreationContext> getConstraintViolationCreationContexts() {
		if ( defaultDisabled && constraintViolationCreationContexts.size() == 0 ) {
			throw log.getAtLeastOneCustomMessageMustBeCreatedException();
		}

		List<ConstraintViolationCreationContext> returnedConstraintViolationCreationContexts = new ArrayList<ConstraintViolationCreationContext>(
				constraintViolationCreationContexts
		);
		if ( !defaultDisabled ) {
			Map<String, Object> parameterMapCopy = newHashMap();
			parameterMapCopy.putAll( expressionVariables );
			returnedConstraintViolationCreationContexts.add(
					new ConstraintViolationCreationContext(
							getDefaultConstraintMessageTemplate(),
							basePath,
							parameterMapCopy
					)
			);
		}
		return returnedConstraintViolationCreationContexts;
	}

	public List<String> getMethodParameterNames() {
		return methodParameterNames;
	}

	private abstract class NodeBuilderBase {
		protected final String messageTemplate;
		protected PathImpl propertyPath;

		protected NodeBuilderBase(String template, PathImpl path) {
			this.messageTemplate = template;
			this.propertyPath = path;
		}

		public ConstraintValidatorContext addConstraintViolation() {
			Map<String, Object> parameterMapCopy = newHashMap();
			parameterMapCopy.putAll( expressionVariables );
			constraintViolationCreationContexts.add(
					new ConstraintViolationCreationContext(
							messageTemplate,
							propertyPath,
							parameterMapCopy
					)
			);
			return ConstraintValidatorContextImpl.this;
		}
	}

	private class ConstraintViolationBuilderImpl extends NodeBuilderBase implements ConstraintViolationBuilder {

		private final List<String> methodParameterNames;

		private ConstraintViolationBuilderImpl(List<String> methodParameterNames, String template, PathImpl path) {
			super( template, path );
			this.methodParameterNames = methodParameterNames;
		}

		@Override
		public NodeBuilderDefinedContext addNode(String name) {
			dropLeafNodeIfRequired();
			propertyPath.addPropertyNode( name );

			return new NodeBuilder( messageTemplate, propertyPath );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			dropLeafNodeIfRequired();

			return new DeferredNodeBuilder( messageTemplate, propertyPath, name );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null );
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			if ( propertyPath.getLeafNode().getKind() != ElementKind.CROSS_PARAMETER ) {
				throw log.getParameterNodeAddedForNonCrossParameterConstraintException( propertyPath );
			}

			dropLeafNodeIfRequired();
			propertyPath.addParameterNode( methodParameterNames.get( index ), index );

			return new NodeBuilder( messageTemplate, propertyPath );
		}

		/**
		 * In case nodes are added from within a class-level or cross-parameter
		 * constraint, the node representing the constraint element will be
		 * dropped. inIterable(), getKey() etc.
		 */
		private void dropLeafNodeIfRequired() {
			if ( propertyPath.getLeafNode().getKind() == ElementKind.BEAN || propertyPath.getLeafNode()
					.getKind() == ElementKind.CROSS_PARAMETER ) {
				propertyPath = propertyPath.getPathWithoutLeafNode();
			}
		}
	}

	private class NodeBuilder extends NodeBuilderBase
			implements NodeBuilderDefinedContext, LeafNodeBuilderDefinedContext {

		private NodeBuilder(String template, PathImpl path) {
			super( template, path );
		}

		@Override
		@Deprecated
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null );
		}
	}

	private class DeferredNodeBuilder extends NodeBuilderBase
			implements NodeBuilderCustomizableContext, LeafNodeBuilderCustomizableContext, NodeContextBuilder, LeafNodeContextBuilder {

		private final String leafNodeName;

		private DeferredNodeBuilder(String template, PathImpl path, String nodeName) {
			super( template, path );
			this.leafNodeName = nodeName;
		}

		@Override
		public DeferredNodeBuilder inIterable() {
			propertyPath.makeLeafNodeIterable();
			return this;
		}

		@Override
		public NodeBuilder atKey(Object key) {
			propertyPath.setLeafNodeMapKey( key );
			addLeafNode();
			return new NodeBuilder( messageTemplate, propertyPath );
		}

		@Override
		public NodeBuilder atIndex(Integer index) {
			propertyPath.setLeafNodeIndex( index );
			addLeafNode();
			return new NodeBuilder( messageTemplate, propertyPath );
		}

		@Override
		@Deprecated
		public NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null );
		}

		@Override
		public ConstraintValidatorContext addConstraintViolation() {
			addLeafNode();
			return super.addConstraintViolation();
		}

		/**
		 * Adds the leaf node stored for deferred addition. Either a bean or
		 * property node.
		 */
		private void addLeafNode() {
			if ( leafNodeName == null ) {
				propertyPath.addBeanNode();
			}
			else {
				propertyPath.addPropertyNode( leafNodeName );
			}
		}
	}
}
