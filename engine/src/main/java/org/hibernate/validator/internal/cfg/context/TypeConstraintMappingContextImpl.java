/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
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
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class TypeConstraintMappingContextImpl<C> extends ConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	private static final Log log = LoggerFactory.make();

	private final Class<C> beanClass;

	private final Set<ExecutableConstraintMappingContextImpl> executableContexts = newHashSet();
	private final Set<PropertyConstraintMappingContextImpl> propertyContexts = newHashSet();
	private final Set<Member> configuredMembers = newHashSet();

	private final ExecutableHelper executableHelper;

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

	TypeConstraintMappingContextImpl(DefaultConstraintMapping mapping, Class<C> beanClass, ExecutableHelper executableHelper) {
		super( mapping );
		this.beanClass = beanClass;
		this.executableHelper = executableHelper;
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.FALSE );
	}

	@Override
	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		addConstraint( ConfiguredConstraint.forType( definition, beanClass ) );
		return this;
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
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotNull( elementType, "The element type must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Member member = getMember(
				beanClass, property, elementType
		);

		if ( member == null || member.getDeclaringClass() != beanClass ) {
			throw log.getUnableToFindPropertyWithAccessException( beanClass, property, elementType );
		}

		if ( configuredMembers.contains( member ) ) {
			throw log.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}

		PropertyConstraintMappingContextImpl context = new PropertyConstraintMappingContextImpl(
				this,
				member, executableHelper
		);

		configuredMembers.add( member );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Method method = run( GetDeclaredMethod.action( beanClass, name, parameterTypes ) );

		if ( method == null || method.getDeclaringClass() != beanClass ) {
			throw log.getBeanDoesNotContainMethodException( beanClass, name, Arrays.asList( parameterTypes ) );
		}

		if ( configuredMembers.contains( method ) ) {
			throw log.getMethodHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( name, parameterTypes )
			);
		}

		MethodConstraintMappingContextImpl context = new MethodConstraintMappingContextImpl( this, method, executableHelper );
		configuredMembers.add( method );
		executableContexts.add( context );

		return context;
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		Constructor<C> constructor = run( GetDeclaredConstructor.action( beanClass, parameterTypes ) );

		if ( constructor == null || constructor.getDeclaringClass() != beanClass ) {
			throw log.getBeanDoesNotContainConstructorException(
					beanClass,
					Arrays.asList( parameterTypes )
			);
		}

		if ( configuredMembers.contains( constructor ) ) {
			throw log.getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(
					beanClass,
					ExecutableHelper.getExecutableAsString( beanClass.getSimpleName(), parameterTypes )
			);
		}

		ConstructorConstraintMappingContextImpl context = new ConstructorConstraintMappingContextImpl(
				this,
				constructor,
				executableHelper
		);
		configuredMembers.add( constructor );
		executableContexts.add( context );

		return context;
	}

	BeanConfiguration<C> build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		return new BeanConfiguration<>(
				ConfigurationSource.API,
				beanClass,
				buildConstraintElements( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				defaultGroupSequence,
				getDefaultGroupSequenceProvider()
		);
	}

	private Set<ConstrainedElement> buildConstraintElements(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		Set<ConstrainedElement> elements = newHashSet();

		//class-level configuration
		elements.add(
				new ConstrainedType(
						ConfigurationSource.API,
						beanClass,
						getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager )
				)
		);

		//constructors/methods
		for ( ExecutableConstraintMappingContextImpl executableContext : executableContexts ) {
			elements.add( executableContext.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) );
		}

		//properties
		for ( PropertyConstraintMappingContextImpl propertyContext : propertyContexts ) {
			elements.add( propertyContext.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) );
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
	 * Returns the member with the given name and type.
	 *
	 * @param clazz The class from which to retrieve the member. Cannot be {@code null}.
	 * @param property The property name without "is", "get" or "has". Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return the member which matching the name and type or {@code null} if no such member exists.
	 */
	private Member getMember(Class<?> clazz, String property, ElementType elementType) {
		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.length() == 0 ) {
			throw log.getPropertyNameCannotBeNullOrEmptyException();
		}

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw log.getElementTypeHasToBeFieldOrMethodException();
		}

		Member member = null;
		if ( ElementType.FIELD.equals( elementType ) ) {
			member = run( GetDeclaredField.action( clazz, property ) );
		}
		else {
			member = executableHelper.findMethod( clazz, property );
		}

		return member;
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
