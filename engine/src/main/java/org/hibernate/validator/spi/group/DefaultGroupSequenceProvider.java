/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.group;

import java.util.List;

/**
 * This class defines the dynamic group sequence provider contract.
 * <p>
 * In order to dynamically redefine the default group sequence for a type {@code T},
 * the {@link org.hibernate.validator.group.GroupSequenceProvider} annotation
 * must be placed on {@code T}, specifying as its value a concrete implementation of {@code DefaultGroupSequenceProvider}, which
 * must be parametrized with that same type {@code T}, its subclass {@code Y} ({@code T t; t instanceof Y == true }
 * or an interface {@code I} that is common to the beans on for which this sequence provider is expected to be applied to.
 * <p>
 * If during the validation process the {@code Default} group is validated for {@code T}, the actual validated instance
 * is passed to the {@code DefaultGroupSequenceProvider} to determine the default group sequence.
 * <p>
 * Note:
 * <ul>
 * <li>Implementations must provide a public default constructor.</li>
 * <li>Implementations must be thread-safe.</li>
 * <li>Implementations must return a valid default group sequence,
 * i.e. the returned sequence <b>must</b> contain the bean type itself,
 * which represents the {@link jakarta.validation.groups.Default default group}.</li>
 * </ul>
 *
 * @param <T> The type for which an implementation is defined.
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 */
public interface DefaultGroupSequenceProvider<T> {

	/**
	 * This method returns the default group sequence for the given {@code klass} bean type and {@code object} instance.
	 * <p>
	 * The object parameter allows to dynamically compose the default group sequence based on the state of the validated value.
	 *
	 * @param klass the type of the bean for which the group sequence is requested.
	 * @param object the instance under validation. This value <b>can</b> be {@code null}, e.g. in case this method was called as part of
	 * {@linkplain jakarta.validation.Validator#validateValue(Class, String, Object, Class[]) Validator#validateValue}.
	 * @return a list of classes specifying the default group sequence. The same constraints to the redefined group list
	 * apply as for lists defined via {@code GroupSequence}. In particular the list has to contain the type T.
	 */
	default List<Class<?>> getValidationGroups(Class<?> klass, T object) {
		return getValidationGroups( object );
	}

	/**
	 * This method returns the default group sequence for the given instance.
	 * <p>
	 * The object parameter allows to dynamically compose the default group sequence in function of the validated value state.
	 * </p>
	 *
	 * @param object the instance being validated. This value can be {@code null} in case this method was called as part of
	 * {@linkplain jakarta.validation.Validator#validateValue(Class, String, Object, Class[]) Validator#validateValue}.
	 * @return a list of classes specifying the default group sequence. The same constraints to the redefined group list
	 * apply as for lists defined via {@code GroupSequence}. In particular the list has to contain the type T.
	 * @deprecated Use the {@link #getValidationGroups(Class, Object)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.0.0")
	default List<Class<?>> getValidationGroups(T object) {
		throw new AssertionError( "Unexpected call to get the validation group sequence. " + "The variant receiving the bean type must be used by Hibernate Validator" );
	}
}
