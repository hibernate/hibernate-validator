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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.internal.properties.javabean.JavaBeanExecutable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
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
 */
public final class TypeConstraintMappingContextImpl<C> extends ConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<C> beanClass;

	private final Set<ExecutableConstraintMappingContextImpl> executableContexts = newHashSet();
	private final Set<PropertyConstraintMappingContextImpl> propertyContexts = newHashSet();
	private final Set<Constrainable> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

	TypeConstraintMappingContextImpl(DefaultConstraintMapping mapping, Class<C> beanClass) {
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
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotNull( elementType, "The element type must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Property foundProperty = getProperty(
				beanClass, property, elementType
		);

		if ( foundProperty == null || foundProperty.getDeclaringClass() != beanClass ) {
			throw LOG.getUnableToFindPropertyWithAccessException( beanClass, property, elementType );
		}

		if ( configuredMembers.contains( foundProperty ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( beanClass, property );
		}

		PropertyConstraintMappingContextImpl context = PropertyConstraintMappingContextImpl.context( elementType, this, foundProperty );
		configuredMembers.add( foundProperty );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Method method = run( GetDeclaredMethod.action( beanClass, name, parameterTypes ) );

		if ( method == null || method.getDeclaringClass() != beanClass ) {
			throw LOG.getBeanDoesNotContainMethodException( beanClass, name, parameterTypes );
		}

		JavaBeanMethod javaBeanMethod = JavaBeanExecutable.of( method );

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
		Constructor<C> constructor = run( GetDeclaredConstructor.action( beanClass, parameterTypes ) );

		if ( constructor == null || constructor.getDeclaringClass() != beanClass ) {
			throw LOG.getBeanDoesNotContainConstructorException(
					beanClass,
					parameterTypes
			);
		}

		JavaBeanConstructor javaBeanConstructor = new JavaBeanConstructor( constructor );

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
	 * Returns the property with the given name and type.
	 *
	 * @param clazz The class from which to retrieve the property. Cannot be {@code null}.
	 * @param property The property name without "is", "get" or "has". Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return the property which matches the name and type or {@code null} if no such property exists.
	 */
	private Property getProperty(Class<?> clazz, String property, ElementType elementType) {
		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.length() == 0 ) {
			throw LOG.getPropertyNameCannotBeNullOrEmptyException();
		}

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw LOG.getElementTypeHasToBeFieldOrMethodException();
		}

		if ( ElementType.FIELD.equals( elementType ) ) {
			Field field = run( GetDeclaredField.action( clazz, property ) );
			return field == null ? null : new JavaBeanField( field );
		}
		else {
			Method method = null;
			String methodName = property.substring( 0, 1 ).toUpperCase( Locale.ROOT ) + property.substring( 1 );
			for ( String prefix : ReflectionHelper.PROPERTY_ACCESSOR_PREFIXES ) {
				method = run( GetMethod.action( clazz, prefix + methodName ) );
				if ( method != null ) {
					break;
				}
			}
			return method == null ? null : new JavaBeanGetter( method );
		}
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
