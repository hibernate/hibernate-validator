/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class TypeVariableBindingsTest {

	@Test
	public void canObtainBindingForList() {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> bindings = TypeVariableBindings.getTypeVariableBindings( List.class );

		Map<TypeVariable<?>, TypeVariable<?>> listBindings = bindings.get( List.class );
		assertThat( listBindings ).containsOnly(
			entry( List.class.getTypeParameters()[0], List.class.getTypeParameters()[0] )
		);
	}

	@Test
	public void canObtainBindingForHashMap() {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> bindings = TypeVariableBindings.getTypeVariableBindings( HashMap.class );

		Map<TypeVariable<?>, TypeVariable<?>> hashMapBindings = bindings.get( HashMap.class );
		assertThat( hashMapBindings ).containsOnly(
			entry( HashMap.class.getTypeParameters()[0], HashMap.class.getTypeParameters()[0] ),
			entry( HashMap.class.getTypeParameters()[1], HashMap.class.getTypeParameters()[1] )
		);

		Map<TypeVariable<?>, TypeVariable<?>> mapBindings = bindings.get( Map.class );
		assertThat( mapBindings ).containsOnly(
			entry( HashMap.class.getTypeParameters()[0], Map.class.getTypeParameters()[0] ),
			entry( HashMap.class.getTypeParameters()[1], Map.class.getTypeParameters()[1] )
		);
	}

	@Test
	public void canObtainBindingForMultimapAndListTypeWithIntermediateType() {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> bindings = TypeVariableBindings.getTypeVariableBindings( WeirdMap.class );

		Map<TypeVariable<?>, TypeVariable<?>> weirdMapBindings = bindings.get( WeirdMap.class );
		assertThat( weirdMapBindings ).containsOnly(
			entry( WeirdMap.class.getTypeParameters()[0], WeirdMap.class.getTypeParameters()[0] ),
			entry( WeirdMap.class.getTypeParameters()[1], WeirdMap.class.getTypeParameters()[1] ),
			entry( WeirdMap.class.getTypeParameters()[2], WeirdMap.class.getTypeParameters()[2] )
		);

		Map<TypeVariable<?>, TypeVariable<?>> multimapBindings = bindings.get( Multimap.class );
		assertThat( multimapBindings ).containsOnly(
				// KEY -> K
				entry( WeirdMap.class.getTypeParameters()[1], Multimap.class.getTypeParameters()[0] ),
				// VALUE -> V
				entry( WeirdMap.class.getTypeParameters()[2], Multimap.class.getTypeParameters()[1] )
		);

		Map<TypeVariable<?>, TypeVariable<?>> middleMapBindings = bindings.get( MiddleMap.class );
		assertThat( middleMapBindings ).containsOnly(
				// KEY -> S
				entry( WeirdMap.class.getTypeParameters()[1], MiddleMap.class.getTypeParameters()[0] ),
				// VALUE -> W
				entry( WeirdMap.class.getTypeParameters()[2], MiddleMap.class.getTypeParameters()[1] )
		);

		Map<TypeVariable<?>, TypeVariable<?>> listBindings = bindings.get( List.class );
		assertThat( listBindings ).containsOnly(
			entry( WeirdMap.class.getTypeParameters()[2], List.class.getTypeParameters()[0] )
		);

		Map<TypeVariable<?>, TypeVariable<?>> iterableBindings = bindings.get( Iterable.class );
		assertThat( iterableBindings ).containsOnly(
			entry( WeirdMap.class.getTypeParameters()[2], Iterable.class.getTypeParameters()[0] )
		);
	}

	private interface Multimap<K, V> {
	}

	private interface MiddleMap<S, W> extends Multimap<S, W>, List<W> {
	}

	private interface WeirdMap<ID, KEY, VALUE> extends MiddleMap<KEY, VALUE> {
	}
}
