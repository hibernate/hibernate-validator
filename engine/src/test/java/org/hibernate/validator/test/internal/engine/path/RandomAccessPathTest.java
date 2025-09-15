/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Path;

import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.path.RandomAccessPath;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class RandomAccessPathTest {

	@Test
	public void testParsing() {
		String property = "orders[3].deliveryAddress.addressline[1]";
		Path path = MutablePath.createPathFromString( property ).materialize();
		assertThat( path ).isInstanceOf( RandomAccessPath.class );

		if ( path instanceof RandomAccessPath randomAccessPath ) {
			assertThat( randomAccessPath.length() ).isEqualTo( 4 );

			// Get the root node and assert its properties
			Path.Node elem = randomAccessPath.getRootNode();
			assertThat( elem ).isNotNull();
			assertThat( elem.getName() ).isEqualTo( "orders" );
			assertThat( elem.isInIterable() ).isFalse();

			// Get the node by index 0 and assert its properties
			elem = randomAccessPath.getNode( 0 );
			assertThat( elem ).isNotNull();
			assertThat( elem.getName() ).isEqualTo( "orders" );
			assertThat( elem.isInIterable() ).isFalse();

			// Get the node by index 1 and assert its properties
			elem = randomAccessPath.getNode( 1 );
			assertThat( elem ).isNotNull();
			assertThat( elem.getName() ).isEqualTo( "deliveryAddress" );
			assertThat( elem.isInIterable() ).isTrue();
			assertThat( elem.getIndex() ).isEqualTo( 3 );

			// Get the node by index 2 and assert its properties
			elem = randomAccessPath.getNode( 2 );
			assertThat( elem ).isNotNull();
			assertThat( elem.getName() ).isEqualTo( "addressline" );
			assertThat( elem.isInIterable() ).isFalse();

			// Get the node by index 3 and assert its properties
			elem = randomAccessPath.getNode( 3 );
			assertThat( elem ).isNotNull();
			assertThat( elem.getName() ).isNull();
			assertThat( elem.isInIterable() ).isTrue();
			assertThat( elem.getIndex() ).isEqualTo( 1 );

			assertThatThrownBy( () -> randomAccessPath.getNode( 4 ) )
					.isInstanceOf( IndexOutOfBoundsException.class );

			assertThat( path.toString() ).isEqualTo( property );
		}
		else {
			Assertions.fail( "Path is not IndexedPath" );
		}
	}
}
