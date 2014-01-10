/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.cfg.DefaultConstraintMapping;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @param <C> The type represented by this creational context.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class TypeConstraintMappingContextImpl<C> extends ConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	private static final Log log = LoggerFactory.make();

	private final Class<C> beanClass;

	private final Set<ExecutableConstraintMappingContextImpl> executableContexts = newHashSet();
	private final Set<PropertyConstraintMappingContextImpl> propertyContexts = newHashSet();
	private final Set<Member> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

	public TypeConstraintMappingContextImpl(DefaultConstraintMapping mapping, Class<C> beanClass) {
		super( mapping );
		this.beanClass = beanClass;
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.FALSE );
	}

	@Override
	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		addConstraint( ConfiguredConstraint.forType( definition, beanClass ) );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> ignoreAnnotations() {
		mapping.getAnnotationProcessingOptions().ignoreClassLevelConstraintAnnotations( beanClass, Boolean.TRUE );
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
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotNull( elementType, "The element type must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Member member = ReflectionHelper.getMember(
				beanClass, property, elementType
		);

		if ( member == null || member.getDeclaringClass() != beanClass ) {
			throw log.getUnableToFindPropertyWithAccessException( beanClass, property, elementType );
		}

		if ( configuredMembers.contains( member ) ) {
			throw log.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass.getName(), property );
		}

		PropertyConstraintMappingContextImpl context = new PropertyConstraintMappingContextImpl(
				this,
				member
		);

		configuredMembers.add( member );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Method method = ReflectionHelper.getDeclaredMethod( beanClass, name, parameterTypes );

		if ( method == null || method.getDeclaringClass() != beanClass ) {
			throw log.getUnableToFindMethodException(
					beanClass,
					ExecutableElement.getExecutableAsString( name, parameterTypes )
			);
		}

		if ( configuredMembers.contains( method ) ) {
			throw log.getMethodHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass.getName(),
					ExecutableElement.getExecutableAsString( name, parameterTypes )
			);
		}

		ExecutableConstraintMappingContextImpl context = new ExecutableConstraintMappingContextImpl( this, method );
		configuredMembers.add( method );
		executableContexts.add( context );

		return context;
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		Constructor<C> constructor = ReflectionHelper.getDeclaredConstructor( beanClass, parameterTypes );

		if ( constructor == null || constructor.getDeclaringClass() != beanClass ) {
			throw log.getBeanDoesNotContainConstructorException(
					beanClass.getName(),
					StringHelper.join( parameterTypes, ", " )
			);
		}

		if ( configuredMembers.contains( constructor ) ) {
			throw log.getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass.getName(),
					ExecutableElement.getExecutableAsString( beanClass.getSimpleName(), parameterTypes )
			);
		}

		ExecutableConstraintMappingContextImpl context = new ExecutableConstraintMappingContextImpl(
				this,
				constructor
		);
		configuredMembers.add( constructor );
		executableContexts.add( context );

		return context;
	}

	public BeanConfiguration<C> build(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		return new BeanConfiguration<C>(
				ConfigurationSource.API,
				beanClass,
				buildConstraintElements( constraintHelper, parameterNameProvider ),
				defaultGroupSequence,
				getDefaultGroupSequenceProvider()
		);
	}

	private Set<ConstrainedElement> buildConstraintElements(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		Set<ConstrainedElement> elements = newHashSet();

		//class-level configuration
		elements.add(
				new ConstrainedType(
						ConfigurationSource.API,
						ConstraintLocation.forClass( beanClass ),
						getConstraints( constraintHelper )
				)
		);

		//constructors/methods
		for ( ExecutableConstraintMappingContextImpl executableContext : executableContexts ) {
			elements.add( executableContext.build( constraintHelper, parameterNameProvider ) );
		}

		//properties
		for ( PropertyConstraintMappingContextImpl propertyContext : propertyContexts ) {
			elements.add( propertyContext.build( constraintHelper ) );
		}

		return elements;
	}

	private DefaultGroupSequenceProvider<? super C> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProviderClass != null ? ReflectionHelper.newInstance(
				defaultGroupSequenceProviderClass,
				"default group sequence provider"
		) : null;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
