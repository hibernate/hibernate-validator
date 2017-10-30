/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeContextBuilder;
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
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ConstraintValidatorContextImpl implements HibernateConstraintValidatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private Map<String, Object> messageParameters;
	private Map<String, Object> expressionVariables;
	private final List<String> methodParameterNames;
	private final ClockProvider clockProvider;
	private final PathImpl basePath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private List<ConstraintViolationCreationContext> constraintViolationCreationContexts;
	private boolean defaultDisabled;
	private Object dynamicPayload;

	public ConstraintValidatorContextImpl(List<String> methodParameterNames, ClockProvider clockProvider,
			PathImpl propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
		this.methodParameterNames = methodParameterNames;
		this.clockProvider = clockProvider;
		this.basePath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
	}

	@Override
	public final void disableDefaultConstraintViolation() {
		defaultDisabled = true;
	}

	@Override
	public final String getDefaultConstraintMessageTemplate() {
		return constraintDescriptor.getMessageTemplate();
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
		throw LOG.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateConstraintValidatorContext addExpressionVariable(String name, Object value) {
		Contracts.assertNotNull( name, "null is not a valid value for an expression variable name" );

		if ( expressionVariables == null ) {
			expressionVariables = new HashMap<>();
		}

		this.expressionVariables.put( name, value );
		return this;
	}

	@Override
	public HibernateConstraintValidatorContext addMessageParameter(String name, Object value) {
		Contracts.assertNotNull( name, "null is not a valid value for a parameter name" );

		if ( messageParameters == null ) {
			messageParameters = new HashMap<>();
		}

		this.messageParameters.put( name, value );
		return this;
	}

	@Override
	public ClockProvider getClockProvider() {
		return clockProvider;
	}

	@Override
	public HibernateConstraintValidatorContext withDynamicPayload(Object violationContext) {
		this.dynamicPayload = violationContext;
		return this;
	}

	public final ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public final List<ConstraintViolationCreationContext> getConstraintViolationCreationContexts() {
		if ( defaultDisabled ) {
			if ( constraintViolationCreationContexts == null || constraintViolationCreationContexts.size() == 0 ) {
				throw LOG.getAtLeastOneCustomMessageMustBeCreatedException();
			}

			return CollectionHelper.toImmutableList( constraintViolationCreationContexts );
		}

		if ( constraintViolationCreationContexts == null || constraintViolationCreationContexts.size() == 0 ) {
			return Collections.singletonList( getDefaultConstraintViolationCreationContext() );
		}

		List<ConstraintViolationCreationContext> returnedConstraintViolationCreationContexts =
				new ArrayList<>( constraintViolationCreationContexts.size() + 1 );
		returnedConstraintViolationCreationContexts.addAll( constraintViolationCreationContexts );
		returnedConstraintViolationCreationContexts.add( getDefaultConstraintViolationCreationContext() );

		return CollectionHelper.toImmutableList( returnedConstraintViolationCreationContexts );
	}

	private ConstraintViolationCreationContext getDefaultConstraintViolationCreationContext() {
		return new ConstraintViolationCreationContext(
				getDefaultConstraintMessageTemplate(),
				basePath,
				messageParameters != null ? new HashMap<>( messageParameters ) : Collections.emptyMap(),
				expressionVariables != null ? new HashMap<>( expressionVariables ) : Collections.emptyMap(),
				dynamicPayload
		);
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
			if ( constraintViolationCreationContexts == null ) {
				constraintViolationCreationContexts = CollectionHelper.newArrayList( 3 );
			}
			constraintViolationCreationContexts.add(
					new ConstraintViolationCreationContext(
							messageTemplate,
							propertyPath,
							messageParameters != null ? new HashMap<>( messageParameters ) : Collections.emptyMap(),
							expressionVariables != null ? new HashMap<>( expressionVariables ) : Collections.emptyMap(),
							dynamicPayload
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

			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			if ( propertyPath.getLeafNode().getKind() != ElementKind.CROSS_PARAMETER ) {
				throw LOG.getParameterNodeAddedForNonCrossParameterConstraintException( propertyPath );
			}

			dropLeafNodeIfRequired();
			propertyPath.addParameterNode( methodParameterNames.get( index ), index );

			return new NodeBuilder( messageTemplate, propertyPath );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			dropLeafNodeIfRequired();

			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, containerType, typeArgumentIndex );
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
			implements NodeBuilderDefinedContext, LeafNodeBuilderDefinedContext, ContainerElementNodeBuilderDefinedContext {

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
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, containerType, typeArgumentIndex );
		}
	}

	private class DeferredNodeBuilder extends NodeBuilderBase
			implements NodeBuilderCustomizableContext, LeafNodeBuilderCustomizableContext, NodeContextBuilder, LeafNodeContextBuilder,
			ContainerElementNodeBuilderCustomizableContext, ContainerElementNodeContextBuilder {

		private final String leafNodeName;

		private final ElementKind leafNodeKind;

		private final Class<?> leafNodeContainerType;

		private final Integer leafNodeTypeArgumentIndex;

		private DeferredNodeBuilder(String template, PathImpl path, String nodeName, ElementKind leafNodeKind) {
			super( template, path );
			this.leafNodeName = nodeName;
			this.leafNodeKind = leafNodeKind;
			this.leafNodeContainerType = null;
			this.leafNodeTypeArgumentIndex = null;
		}

		private DeferredNodeBuilder(String template, PathImpl path, String nodeName, Class<?> leafNodeContainerType, Integer leafNodeTypeArgumentIndex) {
			super( template, path );
			this.leafNodeName = nodeName;
			this.leafNodeKind = ElementKind.CONTAINER_ELEMENT;
			this.leafNodeContainerType = leafNodeContainerType;
			this.leafNodeTypeArgumentIndex = leafNodeTypeArgumentIndex;
		}

		@Override
		public DeferredNodeBuilder inIterable() {
			propertyPath.makeLeafNodeIterable();
			return this;
		}

		@Override
		public DeferredNodeBuilder inContainer(Class<?> containerClass, Integer typeArgumentIndex) {
			propertyPath.setLeafNodeTypeParameter( containerClass, typeArgumentIndex );
			return this;
		}

		@Override
		public NodeBuilder atKey(Object key) {
			propertyPath.makeLeafNodeIterableAndSetMapKey( key );
			addLeafNode();
			return new NodeBuilder( messageTemplate, propertyPath );
		}

		@Override
		public NodeBuilder atIndex(Integer index) {
			propertyPath.makeLeafNodeIterableAndSetIndex( index );
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
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, propertyPath, name, containerType, typeArgumentIndex );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, propertyPath, null, ElementKind.BEAN );
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
			switch ( leafNodeKind ) {
				case BEAN:
					propertyPath.addBeanNode();
					break;
				case PROPERTY:
					propertyPath.addPropertyNode( leafNodeName );
					break;
				case CONTAINER_ELEMENT:
					propertyPath.setLeafNodeTypeParameter( leafNodeContainerType, leafNodeTypeArgumentIndex );
					propertyPath.addContainerElementNode( leafNodeName );
					break;
				default:
					throw new IllegalStateException( "Unsupported node kind: " + leafNodeKind );
			}
		}
	}
}
