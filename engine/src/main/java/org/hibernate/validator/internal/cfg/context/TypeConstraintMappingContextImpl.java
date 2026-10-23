/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.actions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.actions.GetDeclaredField;
import org.hibernate.validator.internal.util.actions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
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

	private final Class<C> beanClass;

	private final List<MemberContext> memberContexts = new ArrayList<>();
	private final Set<Member> configuredMembers = newHashSet();
	private final Set<String> configuredGetters = newHashSet();
	private boolean ignoreAllAnnotations;

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

	TypeConstraintMappingContextImpl(DefaultConstraintMapping mapping, Class<C> beanClass) {
		super( mapping );
		this.beanClass = beanClass;
	}

	@Override
	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		addConstraint( definition );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> ignoreAnnotations(boolean ignoreAnnotations) {
		this.ignoreAnnotations = ignoreAnnotations;
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> ignoreAllAnnotations() {
		this.ignoreAllAnnotations = true;
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequence(Class<?>... defaultGroupSequence) {
		this.defaultGroupSequence = Arrays.asList( defaultGroupSequence.clone() );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequenceProviderClass(Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass) {
		this.defaultGroupSequenceProviderClass = defaultGroupSequenceProviderClass;
		return this;
	}

	@Override
	public PropertyConstraintMappingContext field(String property) {
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Field field = GetDeclaredField.action( beanClass, property );

		if ( field == null ) {
			throw LOG.getUnableToFindPropertyWithAccessException( beanClass, property, ElementType.FIELD );
		}

		if ( configuredMembers.contains( field ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}

		FieldConstraintMappingContextImpl context = new FieldConstraintMappingContextImpl( this, field );
		configuredMembers.add( field );
		memberContexts.add( context );
		return context;
	}

	@Override
	public PropertyConstraintMappingContext getter(String property) {
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		if ( !configuredGetters.add( property ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}
		GetterConstraintMappingContextImpl context = new GetterConstraintMappingContextImpl( this, property );
		memberContexts.add( context );
		return context;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Method method = GetDeclaredMethod.action( beanClass, name, parameterTypes );

		if ( method == null ) {
			throw LOG.getBeanDoesNotContainMethodException( beanClass, name, parameterTypes );
		}

		if ( configuredMembers.contains( method ) ) {
			throw LOG.getMethodHasAlreadyBeenConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( name, parameterTypes )
			);
		}

		MethodConstraintMappingContextImpl context = new MethodConstraintMappingContextImpl( this, method );
		configuredMembers.add( method );
		memberContexts.add( context );

		return context;
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		Constructor<C> constructor = GetDeclaredConstructor.action( beanClass, parameterTypes );

		if ( constructor == null ) {
			throw LOG.getBeanDoesNotContainConstructorException(
					beanClass,
					parameterTypes
			);
		}

		if ( configuredMembers.contains( constructor ) ) {
			throw LOG.getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( beanClass.getSimpleName(), parameterTypes )
			);
		}

		ConstructorConstraintMappingContextImpl context = new ConstructorConstraintMappingContextImpl(
				this,
				constructor
		);
		configuredMembers.add( constructor );
		memberContexts.add( context );

		return context;
	}

	ResolvedConstraintMapping.ResolvedType<C> materialize(JavaBeanHelper helper,
			AnnotationProcessingOptionsImpl options) {
		// Recording this unconditionally - and not only when ignoreAllAnnotations() was called - makes a programmatic
		// mapping override the ignore-annotations default of an XML <bean> element for the same type. That is
		// questionable, but it is the long-standing behavior and changing it would require a dedicated ticket.
		options.ignoreAnnotationConstraintForClass( beanClass, ignoreAllAnnotations );
		if ( ignoreAnnotations != null ) {
			options.ignoreClassLevelConstraintAnnotations( beanClass, ignoreAnnotations );
		}
		Set<Constrainable> resolvedMembers = newHashSet();
		List<ResolvedConstraintMapping.ResolvedElement> members = new ArrayList<>();
		for ( MemberContext member : memberContexts ) {
			members.add( member.materialize( helper, options, resolvedMembers ) );
		}
		return new ResolvedConstraintMapping.ResolvedType<>( beanClass,
				materializeConstraints( ConstraintLocation.forClass( beanClass ) ),
				members, defaultGroupSequence, defaultGroupSequenceProviderClass );
	}

	interface MemberContext {
		ResolvedConstraintMapping.ResolvedElement materialize(JavaBeanHelper helper,
				AnnotationProcessingOptionsImpl options, Set<Constrainable> resolvedMembers);
	}

	Class<?> getBeanClass() {
		return beanClass;
	}

}
