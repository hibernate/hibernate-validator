/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ElementKind;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.hibernate.validator.constraintvalidation.HibernateCrossParameterConstraintValidatorContext;
import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class HibernateConstraintValidatorReusableContext implements HibernateCrossParameterConstraintValidatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ClockProvider clockProvider;
	private final ExpressionLanguageFeatureLevel defaultConstraintExpressionLanguageFeatureLevel;
	private final ExpressionLanguageFeatureLevel defaultCustomViolationExpressionLanguageFeatureLevel;
	private final Object constraintValidatorPayload;

	private Map<String, Object> messageParameters;
	private Map<String, Object> expressionVariables;
	private boolean defaultDisabled;
	private Object dynamicPayload;
	private List<String> parameterNames;

	private MutablePath basePath;
	private ConstraintDescriptor<?> constraintDescriptor;
	private ContextKind contextKind;

	private List<ConstraintViolationCreationContext> constraintViolationCreationContexts;

	public HibernateConstraintValidatorReusableContext(
			ClockProvider clockProvider,
			Object constraintValidatorPayload,
			ExpressionLanguageFeatureLevel defaultConstraintExpressionLanguageFeatureLevel,
			ExpressionLanguageFeatureLevel defaultCustomViolationExpressionLanguageFeatureLevel) {
		this.clockProvider = clockProvider;
		this.defaultConstraintExpressionLanguageFeatureLevel = defaultConstraintExpressionLanguageFeatureLevel;
		this.defaultCustomViolationExpressionLanguageFeatureLevel = defaultCustomViolationExpressionLanguageFeatureLevel;
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
		return new HibernateConstraintValidatorReusableContext.ConstraintViolationBuilderImpl(
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
		if ( ContextKind.CROSS_PARAMETER.equals( contextKind )
				&& type.isAssignableFrom( HibernateCrossParameterConstraintValidatorContext.class ) ) {
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
			if ( constraintViolationCreationContexts == null || constraintViolationCreationContexts.isEmpty() ) {
				throw LOG.getAtLeastOneCustomMessageMustBeCreatedException();
			}

			return constraintViolationCreationContexts;
		}

		if ( constraintViolationCreationContexts == null || constraintViolationCreationContexts.isEmpty() ) {
			return Collections.singletonList( getDefaultConstraintViolationCreationContext() );
		}

		List<ConstraintViolationCreationContext> returnedConstraintViolationCreationContexts =
				new ArrayList<>( constraintViolationCreationContexts.size() + 1 );
		returnedConstraintViolationCreationContexts.addAll( constraintViolationCreationContexts );
		returnedConstraintViolationCreationContexts.add( getDefaultConstraintViolationCreationContext() );

		return returnedConstraintViolationCreationContexts;
	}

	protected final MutablePath getCopyOfBasePath() {
		return MutablePath.createCopy( basePath );
	}

	private ConstraintViolationCreationContext getDefaultConstraintViolationCreationContext() {
		return new ConstraintViolationCreationContext(
				constraintDescriptor,
				getDefaultConstraintMessageTemplate(),
				defaultConstraintExpressionLanguageFeatureLevel,
				false,
				basePath,
				messageParameters != null ? Map.copyOf( messageParameters ) : Collections.emptyMap(),
				expressionVariables != null ? Map.copyOf( expressionVariables ) : Collections.emptyMap(),
				dynamicPayload
		);
	}

	@Override
	public List<String> getMethodParameterNames() {
		if ( ContextKind.CROSS_PARAMETER.equals( contextKind ) ) {
			return parameterNames;
		}
		throw LOG.getUnexpectedConstraintValidatorContextCall();
	}

	public void supplyConstraintViolationCreationContexts(Collection<ConstraintViolationCreationContext> contexts) {
		if ( defaultDisabled ) {
			if ( this.constraintViolationCreationContexts == null || this.constraintViolationCreationContexts.isEmpty() ) {
				throw LOG.getAtLeastOneCustomMessageMustBeCreatedException();
			}
		}
		else {
			contexts.add( getDefaultConstraintViolationCreationContext() );
		}

		if ( this.constraintViolationCreationContexts != null && !this.constraintViolationCreationContexts.isEmpty() ) {
			contexts.addAll( this.constraintViolationCreationContexts );
		}
	}

	private abstract class NodeBuilderBase {

		protected final String messageTemplate;
		protected ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel;
		protected MutablePath propertyPath;

		protected NodeBuilderBase(String template, MutablePath path) {
			this( template, defaultCustomViolationExpressionLanguageFeatureLevel, path );
		}

		protected NodeBuilderBase(String template, ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel, MutablePath path) {
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
							constraintDescriptor,
							messageTemplate,
							expressionLanguageFeatureLevel,
							true,
							propertyPath,
							messageParameters != null ? Map.copyOf( messageParameters ) : Collections.emptyMap(),
							expressionVariables != null ? Map.copyOf( expressionVariables ) : Collections.emptyMap(),
							dynamicPayload
					)
			);
			return HibernateConstraintValidatorReusableContext.this;
		}
	}

	protected class ConstraintViolationBuilderImpl extends HibernateConstraintValidatorReusableContext.NodeBuilderBase implements HibernateConstraintViolationBuilder {

		protected ConstraintViolationBuilderImpl(String template, MutablePath path) {
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

			return new HibernateConstraintValidatorReusableContext.NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		public NodeBuilderCustomizableContext addPropertyNode(String name) {
			dropLeafNodeIfRequired();

			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public LeafNodeBuilderCustomizableContext addBeanNode() {
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			if ( ContextKind.CROSS_PARAMETER.equals( contextKind ) ) {
				dropLeafNode();
				propertyPath.addParameterNode( parameterNames.get( index ), index );

				return new NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
			}
			throw LOG.getParameterNodeAddedForNonCrossParameterConstraintException( propertyPath );
		}

		@Override
		public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			dropLeafNodeIfRequired();

			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}

		/**
		 * In case nodes are added from within a class-level constraint, the node representing
		 * the constraint element will be dropped. inIterable(), getKey() etc.
		 */
		private void dropLeafNodeIfRequired() {
			if ( propertyPath.getLeafNode().getKind() == ElementKind.BEAN ) {
				propertyPath = MutablePath.createCopyWithoutLeafNode( propertyPath );
			}
			else {
				// if we haven't dropped the node, we should clean up "container-related" things:
				propertyPath.getLeafNode().reset();
			}
		}

		private void dropLeafNode() {
			propertyPath = MutablePath.createCopyWithoutLeafNode( propertyPath );
		}
	}

	protected class NodeBuilder extends HibernateConstraintValidatorReusableContext.NodeBuilderBase
			implements ConstraintViolationBuilder.NodeBuilderDefinedContext, ConstraintViolationBuilder.LeafNodeBuilderDefinedContext,
			ConstraintViolationBuilder.ContainerElementNodeBuilderDefinedContext {

		protected NodeBuilder(String template, ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel, MutablePath path) {
			super( template, expressionLanguageFeatureLevel, path );
		}

		@Override
		@Deprecated
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addPropertyNode(String name) {
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext addBeanNode() {
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
		}

		@Override
		public ConstraintViolationBuilder.ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}
	}

	private class DeferredNodeBuilder extends HibernateConstraintValidatorReusableContext.NodeBuilderBase
			implements ConstraintViolationBuilder.NodeBuilderCustomizableContext, ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext, ConstraintViolationBuilder.NodeContextBuilder,
			ConstraintViolationBuilder.LeafNodeContextBuilder,
			ConstraintViolationBuilder.ContainerElementNodeBuilderCustomizableContext, ConstraintViolationBuilder.ContainerElementNodeContextBuilder {

		private final String leafNodeName;

		private final ElementKind leafNodeKind;

		private final Class<?> leafNodeContainerType;

		private final Integer leafNodeTypeArgumentIndex;

		private DeferredNodeBuilder(String template,
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel,
				MutablePath path,
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
				MutablePath path,
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
		public HibernateConstraintValidatorReusableContext.DeferredNodeBuilder inIterable() {
			propertyPath.makeLeafNodeIterable();
			return this;
		}

		@Override
		public HibernateConstraintValidatorReusableContext.DeferredNodeBuilder inContainer(Class<?> containerClass, Integer typeArgumentIndex) {
			propertyPath.setLeafNodeTypeParameter( containerClass, typeArgumentIndex );
			return this;
		}

		@Override
		public HibernateConstraintValidatorReusableContext.NodeBuilder atKey(Object key) {
			propertyPath.makeLeafNodeIterableAndSetMapKey( key );
			addLeafNode();
			return new HibernateConstraintValidatorReusableContext.NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		public HibernateConstraintValidatorReusableContext.NodeBuilder atIndex(Integer index) {
			propertyPath.makeLeafNodeIterableAndSetIndex( index );
			addLeafNode();
			return new HibernateConstraintValidatorReusableContext.NodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath );
		}

		@Override
		@Deprecated
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addNode(String name) {
			return addPropertyNode( name );
		}

		@Override
		public ConstraintViolationBuilder.NodeBuilderCustomizableContext addPropertyNode(String name) {
			addLeafNode();
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, ElementKind.PROPERTY );
		}

		@Override
		public ConstraintViolationBuilder.ContainerElementNodeBuilderCustomizableContext addContainerElementNode(String name, Class<?> containerType, Integer typeArgumentIndex) {
			addLeafNode();
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, name, containerType, typeArgumentIndex );
		}

		@Override
		public ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext addBeanNode() {
			addLeafNode();
			return new HibernateConstraintValidatorReusableContext.DeferredNodeBuilder( messageTemplate, expressionLanguageFeatureLevel, propertyPath, null, ElementKind.BEAN );
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

	public HibernateConstraintValidatorReusableContext asRegularContext(
			MutablePath propertyPath,
			ConstraintDescriptor<?> constraintDescriptor
	) {
		this.contextKind = ContextKind.REGULAR;
		this.basePath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;

		this.parameterNames = null;
		this.messageParameters = null;
		this.expressionVariables = null;
		this.defaultDisabled = false;
		this.dynamicPayload = null;

		this.constraintViolationCreationContexts = null;

		return this;
	}

	public HibernateConstraintValidatorReusableContext asCrossParameterContext(
			MutablePath propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			List<String> parameterNames
	) {
		Contracts.assertTrue( propertyPath.getLeafNode().getKind() == ElementKind.CROSS_PARAMETER, "Context can only be used for cross parameter validation" );

		this.contextKind = ContextKind.CROSS_PARAMETER;
		this.basePath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
		this.parameterNames = parameterNames;

		this.messageParameters = null;
		this.expressionVariables = null;
		this.defaultDisabled = false;
		this.dynamicPayload = null;

		this.constraintViolationCreationContexts = null;

		return this;
	}

	private enum ContextKind {
		REGULAR, CROSS_PARAMETER;
	}
}
