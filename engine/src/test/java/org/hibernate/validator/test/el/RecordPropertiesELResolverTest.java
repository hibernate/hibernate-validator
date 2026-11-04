/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.StandardELContext;

import org.hibernate.validator.internal.engine.messageinterpolation.el.RecordPropertiesELResolver;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestForIssue(jiraKey = "HV-2283")
public class RecordPropertiesELResolverTest {

	public static Object[][] components() {
		return new Object[][] { { "Alice" }, { null } };
	}

	@ParameterizedTest
	@MethodSource("components")
	public void components_are_resolved_and_read_only(String name) {
		RecordPropertiesELResolver resolver = new RecordPropertiesELResolver();
		ELContext context = new StandardELContext( ExpressionFactory.newInstance() );
		Value value = new Value( name );

		assertThat( resolver.getValue( context, value, "name" ) ).isEqualTo( name );
		assertThat( context.isPropertyResolved() ).isTrue();

		context.setPropertyResolved( false );
		assertThat( resolver.getType( context, value, "name" ) ).isNull();
		assertThat( context.isPropertyResolved() ).isTrue();

		context.setPropertyResolved( false );
		assertThat( resolver.isReadOnly( context, value, "name" ) ).isTrue();
		assertThat( context.isPropertyResolved() ).isTrue();

		context.setPropertyResolved( false );
		assertThatThrownBy( () -> resolver.setValue( context, value, "name", "Bob" ) )
				.isInstanceOf( PropertyNotWritableException.class );
		assertThat( context.isPropertyResolved() ).isTrue();
	}

	public static Object[][] unmatchedProperties() {
		return new Object[][] {
				{ new Value( "Alice" ), "displayName" },
				{ new Value( "Alice" ), "toString" },
				{ new Value( "Alice" ), "missing" },
				{ new Value( "Alice" ), null },
				{ null, "name" },
				{ "Alice", "bytes" }
		};
	}

	@ParameterizedTest
	@MethodSource("unmatchedProperties")
	public void unmatched_properties_are_left_for_other_resolvers(Object base, Object property) {
		RecordPropertiesELResolver resolver = new RecordPropertiesELResolver();
		ELContext context = new StandardELContext( ExpressionFactory.newInstance() );

		assertThat( resolver.getValue( context, base, property ) ).isNull();
		assertThat( context.isPropertyResolved() ).isFalse();
		assertThat( resolver.getType( context, base, property ) ).isNull();
		assertThat( context.isPropertyResolved() ).isFalse();
		assertThat( resolver.isReadOnly( context, base, property ) ).isFalse();
		assertThat( context.isPropertyResolved() ).isFalse();
		resolver.setValue( context, base, property, "Bob" );
		assertThat( context.isPropertyResolved() ).isFalse();
	}

	@Test
	public void null_context_is_rejected() {
		RecordPropertiesELResolver resolver = new RecordPropertiesELResolver();

		assertThatThrownBy( () -> resolver.getValue( null, null, null ) ).isInstanceOf( NullPointerException.class );
		assertThatThrownBy( () -> resolver.getType( null, null, null ) ).isInstanceOf( NullPointerException.class );
		assertThatThrownBy( () -> resolver.isReadOnly( null, null, null ) ).isInstanceOf( NullPointerException.class );
		assertThatThrownBy( () -> resolver.setValue( null, null, null, null ) ).isInstanceOf( NullPointerException.class );
	}

	public record Value(String name) {

		public String getDisplayName() {
			return name;
		}
	}
}
