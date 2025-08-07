/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeBuilderDefinedContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeContextBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderDefinedContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeContextBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeContextBuilder;
import jakarta.validation.ElementKind;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.hibernate.validator.internal.engine.path.ModifiablePath;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ConstraintValidatorContextImpl implements HibernateConstraintValidatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private Map<String, Object> messageParameters;
	private Map<String, Object> expressionVariables;
	private final ClockProvider clockProvider;
	private final ExpressionLanguageFeatureLevel defaultConstraintExpressionLanguageFeatureLevel;
	private final ExpressionLanguageFeatureLevel defaultCustomViolationExpressionLanguageFeatureLevel;
	private final ModifiablePath basePath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private List<ConstraintViolationCreationContext> constraintViolationCreationContexts;
	private boolean defaultDisabled;
	private Object dynamicPayload;
	private final Object constraintValidatorPayload;

	public ConstraintValidatorContextImpl(
			ClockProvider clockProvider,
			ModifiablePath propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			Object constraintValidatorPayload,
			ExpressionLanguageFeatureLevel defaultConstraintExpressionLanguageFeatureLevel,
			ExpressionLanguageFeatureLevel defaultCustomViolationExpressionLanguageFeatureLevel) {
		this.clockProvider = clockProvider;
		this.defaultConstraintExpressionLanguageFeatureLevel = defaultConstraintExpressionLanguageFeatureLevel;
		this.defaultCustomViolationExpressionLanguageFeatureLevel = defaultCustomViolationExpressionLanguageFeatureLevel;
		this.basePath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
		this.constraintValidatorPayload = constraintValidatorPayload;
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
	public HibernateConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return new ConstraintViolationBuilderImpl(
				messageTemplate,
				getCopyOfBasePath()
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

	@Override
	public <C> C getConstraintValidatorPayload(Class<C> type) {
		if ( constraintValidatorPayload != null && type.isAssignableFrom( constraintValidatorPayload.getClass() ) ) {
			return type.cast( constraintValidatorPayload );
		}
		else {
			return null;
		}
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

	protected final ModifiablePath getCopyOfBasePath() {
		return ModifiablePath.createCopy( basePath );
	}

	private ConstraintViolationCreationContext getDefaultConstraintViolationCreationContext() {
		return new ConstraintViolationCreationContext(
				getDefaultConstraintMessageTemplate(),
				defaultConstraintExpressionLanguageFeatureLevel,
				false,
				basePath,
				messageParameters != null ? new HashMap<>( messageParameters ) : Collections.emptyMap(),
				expressionVariables != null ? new HashMap<>( expressionVariables ) : Collections.emptyMap(),
				dynamicPayload
		);
	}

	private abstract class NodeBuilderBase {

		protected final String messageTemplate;
		protected ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel;
		protected ModifiablePath propertyPath;

		protected NodeBuilderBase(String template, ModifiablePath path) {
			this( template, defaultCustomViolationExpressionLanguageFeatureLevel, path );
		}

		protected NodeBuilderBase(String template, ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel, ModifiablePath path) {
			this.messageTemplate = template;
			this.expressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
			this.propertyPath = path;
		}

		public ConstraintValidatorContext addConstraintViolation() {
			if ( constraintViolationCreationContexts == null ) {
				constraintViolationCreationContexts = CollectionHelper.newArrayList( 3 );
			}
			if ( !( expressionVariables == null || expressionVariables.isEmpty() ) && expressionLanguageFeatureLevel == ExpressionLanguageFeatureLevel.NONE ) {
				LOG.expressionVariablesDefinedWithExpressionLanguageNotEnabled(
						constraintDescriptor.getAnnotation() != null ? constraintDescriptor.getAnnotation().annotationType() : Annotation.class );
			}
			constraintViolationCreationContexts.add(
					new ConstraintViolationCreationContext(
							messageTemplate,
							expressionLanguageFeatureLevel,
							true,
							propertyPath,
							messageParameters != null ? new HashMap<>( messageParameters ) : Collections.emptyMap(),
							expressionVariables != null ? new HashMap<>( expressionVariables ) : Collections.emptyMap(),
							dynamicPayload
					)
			);
			return ConstraintValidatorContextImpl.this;
		}
	}

	protected class ConstraintViolationBuilderImpl extends NodeBuilderBase implements HibernateConstraintViolationBuilder {

		protected ConstraintViolationBuilderImpl(String template, ModifiablePath path) {
			super( template, path );
		}

		@Override
		public HibernateConstraintViolationBuilder enableExpressionLanguage(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel) {
			this.expressionLanguageFeatureLevel = ExpressionLanguageFeatureLevel.interpretDefaultForCustomViolations( expressionLanguageFeatureLevel );
			return this;
		}

		@Override
		@Deprecated
		public NodeBuilderDefinedContext addNode(String name) {
			dropLeafNodeIfRequired();
			propertyPath.addPropertyNode( name );

			return new NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			dropLeafNodeIfRequired();

			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			throw LOG.getParameterNodeAddedForNonCrossParameterConstraintException( propertyPath );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			dropLeafNodeIfRequired();

			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}

		/**
		 * In case nodes are added from within a class-level constraint, the node representing
		 * the constraint element will be dropped. inIterable(), getKey() etc.
		 */
		private void dropLeafNodeIfRequired() {
			if ( propertyPath.getLeafNode().getKind() == ElementKind.BEAN ) {
				propertyPath = ModifiablePath.createCopyWithoutLeafNode( propertyPath );
			}
		}
	}

	protected class NodeBuilder extends NodeBuilderBase
			implements NodeBuilderDefinedContext, LeafNodeBuilderDefinedContext, ContainerElementNodeBuilderDefinedContext {

		protected NodeBuilder(String template, ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel, ModifiablePath path) {
			super( template, expressionLanguageFeatureLevel, path );
		}

		@Override
		@Deprecated
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}
	}

	private class DeferredNodeBuilder extends NodeBuilderBase
			implements NodeBuilderCustomizableContext, LeafNodeBuilderCustomizableContext, NodeContextBuilder, LeafNodeContextBuilder,
			ContainerElementNodeBuilderCustomizableContext, ContainerElementNodeContextBuilder {

		private final String leafNodeName;

		private final ElementKind leafNodeKind;

		private final Class<?> leafNodeContainerType;

		private final Integer leafNodeTypeArgumentIndex;

		private DeferredNodeBuilder(String template,
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel,
				ModifiablePath path,
				String nodeName,
				ElementKind leafNodeKind) {
			super( template, expressionLanguageFeatureLevel, path );
			this.leafNodeName = nodeName;
			this.leafNodeKind = leafNodeKind;
			this.leafNodeContainerType = null;
			this.leafNodeTypeArgumentIndex = null;
		}

		private DeferredNodeBuilder(String template,
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel,
				ModifiablePath path,
				String nodeName,
				Class<?> leafNodeContainerType,
				Integer leafNodeTypeArgumentIndex) {
			super( template, expressionLanguageFeatureLevel, path );
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
			return new NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		public NodeBuilder atIndex(Integer index) {
			propertyPath.makeLeafNodeIterableAndSetIndex( index );
			addLeafNode();
			return new NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		@Deprecated
		public NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			addLeafNode();
			return new DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
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
