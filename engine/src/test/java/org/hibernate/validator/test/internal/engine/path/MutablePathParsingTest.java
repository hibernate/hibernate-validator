/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ElementKind;

import org.hibernate.validator.internal.engine.path.MutablePath;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MutablePathParsingTest {
	@DataProvider
	public Object[][] identifiers() {
		return new Object[][] {
				{ "name" },
				{ "$value" },
				{ "_value2" },
				{ "€Amount" },
				{ "höchstBetrag" },
				{ "变量" },
				{ "a\u0301" },
				{ "class" },
				{ "a\u0000b" } };
	}

	@Test(dataProvider = "identifiers")
	public void javaIdentifierNames(String name) {
		var iterator = MutablePath.createPathFromString( name + ".value" ).iterator();
		assertEquals( iterator.next().getName(), name );
		assertEquals( iterator.next().getName(), "value" );
		assertFalse( iterator.hasNext() );
	}

	@DataProvider
	public Object[][] iterableSelectors() {
		return new Object[][] {
				{ "", null, null },
				{ "0", 0, null },
				{ "00001", 1, null },
				{ "2147483647", Integer.MAX_VALUE, null },
				{ "2147483648", null, "2147483648" },
				{ "999999999999999999999999999999999", null, "999999999999999999999999999999999" },
				{ "000000000000000000000000000000001", 1, null },
				{ "key", null, "key" },
				{ "KEY_123", null, "KEY_123" },
				{ "_", null, "_" },
				{ "123abc", null, "123abc" }
		};
	}

	@Test(dataProvider = "iterableSelectors")
	public void selectorOnIntermediateAndTerminalProperty(String selector, Integer index, String key) {
		for ( boolean terminal : new boolean[] { false, true } ) {
			String input = "items[" + selector + "]" + ( terminal ? "" : ".value" );
			var path = MutablePath.createPathFromString( input );
			var iterator = path.iterator();
			var property = iterator.next();
			assertEquals( property.getName(), "items" );
			assertEquals( property.getKind(), ElementKind.PROPERTY );
			assertFalse( property.isInIterable() );
			var element = iterator.next();
			assertEquals( element.getName(), terminal ? null : "value" );
			assertEquals( element.getKind(), terminal ? ElementKind.BEAN : ElementKind.PROPERTY );
			assertTrue( element.isInIterable() );
			assertEquals( element.getIndex(), index );
			assertEquals( element.getKey(), key );
			assertFalse( iterator.hasNext() );
			String normalized = "items[" + ( index != null ? index : selector ) + "]" + ( terminal ? "" : ".value" );
			assertEquals( path.toString(), normalized );
		}
	}

	@DataProvider
	public Object[][] validPaths() {
		return new Object[][] {
				{ "name", "name", List.of( property( "name" ) ) },
				{ "customer.address.city", "customer.address.city", List.of( property( "customer" ), property( "address" ), property( "city" ) ) },
				{ "items[0]", "items[0]", List.of( property( "items" ), bean( 0, null ) ) },
				{ "items[]", "items[]", List.of( property( "items" ), bean( null, null ) ) },
				{ "items[home]", "items[home]", List.of( property( "items" ), bean( null, "home" ) ) },
				{ "customer.items[2].name", "customer.items[2].name", List.of( property( "customer" ), property( "items" ), element( "name", 2, null ) ) },
				{ "items[2].address.city", "items[2].address.city", List.of( property( "items" ), element( "address", 2, null ), property( "city" ) ) },
				{ "customer.items[2]", "customer.items[2]", List.of( property( "customer" ), property( "items" ), bean( 2, null ) ) },
				{ "items[00002].name", "items[2].name", List.of( property( "items" ), element( "name", 2, null ) ) },
				{ "items[2147483648].name", "items[2147483648].name", List.of( property( "items" ), element( "name", null, "2147483648" ) ) },
				{ "items[1].children[2]", "items[1].children[2]", List.of( property( "items" ), element( "children", 1, null ), bean( 2, null ) ) },
				{ "items[].children[].name", "items[].children[].name", List.of( property( "items" ), element( "children", null, null ), element( "name", null, null ) ) },
				{ "items[home].children[work]", "items[home].children[work]", List.of( property( "items" ), element( "children", null, "home" ), bean( null, "work" ) ) },
				{ "$root._items[KEY_1].变量", "$root._items[KEY_1].变量", List.of( property( "$root" ), property( "_items" ), element( "变量", null, "KEY_1" ) ) },
				{ "€Amount.höchstBetrag.a\u0301", "€Amount.höchstBetrag.a\u0301", List.of( property( "€Amount" ), property( "höchstBetrag" ), property( "a\u0301" ) ) },
				{ "class[0].default", "class[0].default", List.of( property( "class" ), element( "default", 0, null ) ) }
		};
	}

	@Test(dataProvider = "validPaths")
	public void validPathHasExpectedNodes(String input, String normalized, List<ExpectedNode> nodes) {
		assertParsedPath( input, normalized, nodes );
	}

	@DataProvider
	public Object[][] selectorCombinations() {
		// Known fragments and their expected values, independent of the parser under test.
		Selector[] selectors = {
				new Selector( "", "", false, null, null ),
				new Selector( "[]", "[]", true, null, null ),
				new Selector( "[0]", "[0]", true, 0, null ),
				new Selector( "[00012]", "[12]", true, 12, null ),
				new Selector( "[home]", "[home]", true, null, "home" ),
				new Selector( "[2147483648]", "[2147483648]", true, null, "2147483648" ),
				new Selector( "[_123]", "[_123]", true, null, "_123" )
		};
		List<Object[]> cases = new ArrayList<>();
		for ( Selector first : selectors ) {
			for ( Selector second : selectors ) {
				for ( boolean terminal : new boolean[] { false, true } ) {
					String input = "orders" + first.text + ".addresses" + second.text + ( terminal ? "" : ".city" );
					String normalized = "orders" + first.normalized + ".addresses" + second.normalized + ( terminal ? "" : ".city" );
					List<ExpectedNode> nodes = new ArrayList<>();
					nodes.add( property( "orders" ) );
					nodes.add( new ExpectedNode( "addresses", ElementKind.PROPERTY, first.iterable, first.index, first.key ) );
					if ( !terminal ) {
						nodes.add( new ExpectedNode( "city", ElementKind.PROPERTY, second.iterable, second.index, second.key ) );
					}
					else if ( second.iterable ) {
						nodes.add( bean( second.index, second.key ) );
					}
					cases.add( new Object[] { input, normalized, nodes } );
				}
			}
		}
		return cases.toArray( Object[][]::new );
	}

	@Test(dataProvider = "selectorCombinations")
	public void combinationsPreserveIterableContext(String input, String normalized, List<ExpectedNode> nodes) {
		assertParsedPath( input, normalized, nodes );
	}

	private static void assertParsedPath(String input, String normalized, List<ExpectedNode> nodes) {
		var path = MutablePath.createPathFromString( input );
		var iterator = path.iterator();
		for ( ExpectedNode expected : nodes ) {
			assertTrue( iterator.hasNext(), input );
			var actual = iterator.next();
			assertEquals( actual.getName(), expected.name, input );
			assertEquals( actual.getKind(), expected.kind, input );
			assertEquals( actual.isInIterable(), expected.iterable, input );
			assertEquals( actual.getIndex(), expected.index, input );
			assertEquals( actual.getKey(), expected.key, input );
		}
		assertFalse( iterator.hasNext(), input );
		assertEquals( path.toString(), normalized, input );
	}

	private static ExpectedNode property(String name) {
		return new ExpectedNode( name, ElementKind.PROPERTY, false, null, null );
	}

	private static ExpectedNode element(String name, Integer index, String key) {
		return new ExpectedNode( name, ElementKind.PROPERTY, true, index, key );
	}

	private static ExpectedNode bean(Integer index, String key) {
		return new ExpectedNode( null, ElementKind.BEAN, true, index, key );
	}

	private record ExpectedNode(String name, ElementKind kind, boolean iterable, Integer index, String key) {
	}

	private record Selector(String text, String normalized, boolean iterable, Integer index, String key) {
	}

	@DataProvider
	public Object[][] malformedPaths() {
		return new Object[][] {
				{ "." },
				{ ".name" },
				{ "name." },
				{ "name..value" },
				{ "name...value" },
				{ "[0]" },
				{ "name[" },
				{ "name[0" },
				{ "name[0]]" },
				{ "name]" },
				{ "name[0]value" },
				{ "name[0][1]" },
				{ "name[[0]]" },
				{ "name.[0]" },
				{ "name[0]." },
				{ "name[0]..value" },
				{ "name[-1]" },
				{ "name[+1]" },
				{ "name[a.b]" },
				{ "name[a-b]" },
				{ "name[a b]" },
				{ "name[$key]" },
				{ "name[é]" },
				{ "name[变量]" },
				{ "name[١]" },
				{ "name[\n]" },
				{ "1name" },
				{ "name.1value" },
				{ "a-b" },
				{ " a" },
				{ "a " },
				{ "name\n.value" },
				{ "name.\nvalue" },
				{ "name.\rvalue" },
				{ "name.\u0085value" },
				{ "name.\u2028value" },
				{ "name.\u2029value" },
				{ "\uD801\uDC00name" },
				{ "name[0]junk.value" }
		};
	}

	@Test(dataProvider = "malformedPaths")
	public void malformedPathRejected(String input) {
		expectThrows( IllegalArgumentException.class, () -> MutablePath.createPathFromString( input ) );
	}

	@Test
	public void rootAndNullPaths() {
		var root = MutablePath.createPathFromString( "" );
		assertEquals( root.toString(), "" );
		var iterator = root.iterator();
		assertNull( iterator.next().getName() );
		assertFalse( iterator.hasNext() );
		expectThrows( IllegalArgumentException.class, () -> MutablePath.createPathFromString( null ) );
	}

	@Test
	public void mixedSelectorsPreserveEachNode() {
		var path = MutablePath.createPathFromString( "orders[2].addresses[home].lines[].text" );
		var iterator = path.iterator();
		assertEquals( iterator.next().getName(), "orders" );
		var address = iterator.next();
		assertEquals( address.getName(), "addresses" );
		assertEquals( address.getIndex(), Integer.valueOf( 2 ) );
		var line = iterator.next();
		assertEquals( line.getName(), "lines" );
		assertEquals( line.getKey(), "home" );
		var text = iterator.next();
		assertEquals( text.getName(), "text" );
		assertTrue( text.isInIterable() );
		assertNull( text.getIndex() );
		assertNull( text.getKey() );
		assertFalse( iterator.hasNext() );
		assertEquals( path.toString(), "orders[2].addresses[home].lines[].text" );
	}

	@Test
	public void deeplyNestedPath() {
		var path = MutablePath.createPathFromString( "node.".repeat( 5000 ) + "leaf[0]" );
		int count = 0;
		for ( var node : path ) {
			assertEquals( node.getName(), count < 5000 ? "node" : count == 5000 ? "leaf" : null );
			count++;
		}
		assertEquals( count, 5002 );
	}
}
