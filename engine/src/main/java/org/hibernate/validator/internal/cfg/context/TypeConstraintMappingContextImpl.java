/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @param <C> The type represented by this creational context.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public final class TypeConstraintMappingContextImpl<C> extends ConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final JavaBeanHelper javaBeanHelper;

	private final Class<C> beanClass;

	private final Set<ExecutableConstraintMappingContextImpl> executableContexts = newHashSet();
	private final Set<AbstractPropertyConstraintMappingContextImpl<?>> propertyContexts = newHashSet();
	private final Set<Constrainable> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

	TypeConstraintMappingContextImpl(JavaBeanHelper javaBeanHelper, DefaultConstraintMapping mapping, Class<C> beanClass) {
		super( mapping );
		this.javaBeanHelper = javaBeanHelper;
		this.beanClass = beanClass;
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.FALSE );
	}

	@Override
	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		addConstraint( ConfiguredConstraint.forType( definition, beanClass ) );
		return this;
	}

	@Override
	@Deprecated
	public TypeConstraintMappingContext<C> ignoreAnnotations() {
		return ignoreAnnotations( true );
	}

	@Override
	public TypeConstraintMappingContext<C> ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreClassLevelConstraintAnnotations( beanClass, ignoreAnnotations );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> ignoreAllAnnotations() {
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.TRUE );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequence(Class<?>... defaultGroupSequence) {
		this.defaultGroupSequence = Arrays.asList( defaultGroupSequence );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequenceProviderClass(Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass) {
		this.defaultGroupSequenceProviderClass = defaultGroupSequenceProviderClass;
		return this;
	}

	@Override
	@Deprecated
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		Contracts.assertNotNull( elementType, "The element type must not be null." );

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw LOG.getElementTypeHasToBeFieldOrMethodException();
		}

		if ( ElementType.FIELD == elementType ) {
			return field( property );
		}
		else {
			return getter( property );
		}
	}

	@Override
	public PropertyConstraintMappingContext field(String property) {
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Optional<JavaBeanField> foundField = javaBeanHelper.findDeclaredField( beanClass, property );

		if ( !foundField.isPresent() ) {
			throw LOG.getUnableToFindPropertyWithAccessException( beanClass, property, ElementType.FIELD );
		}

		JavaBeanField javaBeanField = foundField.get();

		if ( configuredMembers.contains( javaBeanField ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}

		FieldConstraintMappingContextImpl context = new FieldConstraintMappingContextImpl( this, javaBeanField );
		configuredMembers.add( javaBeanField );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public PropertyConstraintMappingContext getter(String property) {
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Optional<JavaBeanGetter> foundGetter = javaBeanHelper.findDeclaredGetter( beanClass, property );

		if ( !foundGetter.isPresent() ) {
			throw LOG.getUnableToFindPropertyWithAccessException( beanClass, property, ElementType.METHOD );
		}

		JavaBeanGetter javaBeanGetter = foundGetter.get();

		if ( configuredMembers.contains( javaBeanGetter ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}

		GetterConstraintMappingContextImpl context = new GetterConstraintMappingContextImpl( this, javaBeanGetter );
		configuredMembers.add( javaBeanGetter );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Optional<JavaBeanMethod> foundMethod = javaBeanHelper.findDeclaredMethod( beanClass, name, parameterTypes );

		if ( !foundMethod.isPresent() ) {
			throw LOG.getBeanDoesNotContainMethodException( beanClass, name, parameterTypes );
		}

		JavaBeanMethod javaBeanMethod = foundMethod.get();

		if ( configuredMembers.contains( javaBeanMethod ) ) {
			throw LOG.getMethodHasAlreadyBeenConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( name, parameterTypes )
			);
		}

		MethodConstraintMappingContextImpl context = new MethodConstraintMappingContextImpl( this, javaBeanMethod );
		configuredMembers.add( javaBeanMethod );
		executableContexts.add( context );

		return context;
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		Optional<JavaBeanConstructor> foundConstructor = javaBeanHelper.findDeclaredConstructor( beanClass, parameterTypes );

		if ( !foundConstructor.isPresent() ) {
			throw LOG.getBeanDoesNotContainConstructorException(
					beanClass,
					parameterTypes
			);
		}

		JavaBeanConstructor javaBeanConstructor = foundConstructor.get();

		if ( configuredMembers.contains( javaBeanConstructor ) ) {
			throw LOG.getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( beanClass.getSimpleName(), parameterTypes )
			);
		}

		ConstructorConstraintMappingContextImpl context = new ConstructorConstraintMappingContextImpl(
				this,
				javaBeanConstructor
		);
		configuredMembers.add( javaBeanConstructor );
		executableContexts.add( context );

		return context;
	}

	BeanConfiguration<C> build(ConstraintCreationContext constraintCreationContext) {
		return new BeanConfiguration<>(
				ConfigurationSource.API,
				beanClass,
				buildConstraintElements( constraintCreationContext ),
				defaultGroupSequence,
				getDefaultGroupSequenceProvider()
		);
	}

	private Set<ConstrainedElement> buildConstraintElements(ConstraintCreationContext constraintCreationContext) {
		Set<ConstrainedElement> elements = newHashSet();

		//class-level configuration
		elements.add(
				new ConstrainedType(
						ConfigurationSource.API,
						beanClass,
						getConstraints( constraintCreationContext )
				)
		);

		//constructors/methods
		for ( ExecutableConstraintMappingContextImpl executableContext : executableContexts ) {
			elements.add( executableContext.build( constraintCreationContext ) );
		}

		//properties
		for ( AbstractPropertyConstraintMappingContextImpl<?> propertyContext : propertyContexts ) {
			elements.add( propertyContext.build( constraintCreationContext ) );
		}

		return elements;
	}

	private DefaultGroupSequenceProvider<? super C> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProviderClass != null ? run(
				NewInstance.action(
						defaultGroupSequenceProviderClass,
						"default group sequence provider"
				)
		) : null;
	}

	Class<?> getBeanClass() {
		return beanClass;
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
