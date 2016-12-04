/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.GroupSequence;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

/**
 * Helper class for working with {@link GroupSequence} and {@link GroupSequenceProvider}.
 *
 * @author Marko Bekhta
 */
public class GroupSequenceJandexHelper {

	protected static final Log log = LoggerFactory.make();

	private final JandexHelper jandexHelper;

	private GroupSequenceJandexHelper(JandexHelper jandexHelper) {
		this.jandexHelper = jandexHelper;
	}

	/**
	 * Creates an instance of a {@link GroupSequenceJandexHelper}.
	 *
	 * @param jandexHelper an instance of {@link JandexHelper}
	 *
	 * @return a new instance of {@link GroupSequenceJandexHelper}
	 */
	public static GroupSequenceJandexHelper getInstance(JandexHelper jandexHelper) {
		return new GroupSequenceJandexHelper( jandexHelper );
	}

	/**
	 * Finds a group sequence if present.
	 *
	 * @param classInfo class to look for {@link GroupSequence} on.
	 *
	 * @return an empty stream if there is no {@link GroupSequence} present, a stream of classes from {@link GroupSequence}'s {@code value} otherwise
	 */
	public Stream<Class<?>> getGroupSequence(ClassInfo classInfo) {
		Optional<AnnotationInstance> groupSequence = jandexHelper.findAnnotation( classInfo.classAnnotations(), GroupSequence.class );
		if ( groupSequence.isPresent() ) {
			return Arrays.stream( (Class<?>[]) jandexHelper.convertAnnotationValue( groupSequence.get().value() ) );
		}
		else {
			return Stream.empty();
		}
	}

	/**
	 * Finds a group sequence provider if present.
	 *
	 * @param classInfo class to look for {@link GroupSequenceProvider} on.
	 *
	 * @return an empty stream if there is no {@link GroupSequence} present, a stream of classes from {@link GroupSequence}'s {@code value} otherwise
	 */
	public <T> DefaultGroupSequenceProvider<? super T> getGroupSequenceProvider(ClassInfo classInfo) {
		Optional<AnnotationInstance> groupSequenceProvider = jandexHelper.findAnnotation( classInfo.classAnnotations(), GroupSequenceProvider.class );
		if ( groupSequenceProvider.isPresent() ) {
			Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass =
					(Class<? extends DefaultGroupSequenceProvider<? super T>>) jandexHelper.convertAnnotationValue( groupSequenceProvider.get().value() );
			return newGroupSequenceProviderClassInstance( jandexHelper.getClassForName( classInfo.name().toString() ), providerClass );
		}
		return null;
	}

	// TODO: next two methods are copied over from AnnotationMetaDataProvider. Is there anything to be done with them ?
	private <T> DefaultGroupSequenceProvider<? super T> newGroupSequenceProviderClassInstance(Class<?> beanClass,
			Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass) {
		Method[] providerMethods = run( GetMethods.action( providerClass ) );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {

				return run(
						NewInstance.action( providerClass, "the default group sequence provider" )
				);
			}
		}

		throw log.getWrongDefaultGroupSequenceProviderTypeException( beanClass );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
