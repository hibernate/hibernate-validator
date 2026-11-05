package org.hibernate.validator.test.internal.engine.path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ElementKind;

import org.hibernate.validator.internal.engine.path.MutablePath;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MutablePathParsingTest {
	public static Object[][] identifiers() {
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

	@ParameterizedTest
	@MethodSource("identifiers")
	public void javaIdentifierNames(String name) {
		var iterator = MutablePath.createPathFromString( name + ".value" ).iterator();
		assertEquals( name, iterator.next().getName() );
		assertEquals( "value", iterator.next().getName() );
		assertFalse( iterator.hasNext() );
	}

	public static Object[][] iterableSelectors() {
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

	@ParameterizedTest
	@MethodSource("iterableSelectors")
	public void selectorOnIntermediateAndTerminalProperty(String selector, Integer index, String key) {
		for ( boolean terminal : new boolean[] { false, true } ) {
			String input = "items[" + selector + "]" + ( terminal ? "" : ".value" );
			var path = MutablePath.createPathFromString( input );
			var iterator = path.iterator();
			var property = iterator.next();
			assertEquals( "items", property.getName() );
			assertEquals( ElementKind.PROPERTY, property.getKind() );
			assertFalse( property.isInIterable() );
			var element = iterator.next();
			assertEquals( terminal ? null : "value", element.getName() );
			assertEquals( terminal ? ElementKind.BEAN : ElementKind.PROPERTY, element.getKind() );
			assertTrue( element.isInIterable() );
			assertEquals( index, element.getIndex() );
			assertEquals( key, element.getKey() );
			assertFalse( iterator.hasNext() );
			String normalized = "items[" + ( index != null ? index : selector ) + "]" + ( terminal ? "" : ".value" );
			assertEquals( normalized, path.toString() );
		}
	}

	public static Object[][] validPaths() {
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

	@ParameterizedTest
	@MethodSource("validPaths")
	public void validPathHasExpectedNodes(String input, String normalized, List<ExpectedNode> nodes) {
		assertParsedPath( input, normalized, nodes );
	}

	public static Object[][] selectorCombinations() {
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

	@ParameterizedTest
	@MethodSource("selectorCombinations")
	public void combinationsPreserveIterableContext(String input, String normalized, List<ExpectedNode> nodes) {
		assertParsedPath( input, normalized, nodes );
	}

	private static void assertParsedPath(String input, String normalized, List<ExpectedNode> nodes) {
		var path = MutablePath.createPathFromString( input );
		var iterator = path.iterator();
		for ( ExpectedNode expected : nodes ) {
			assertTrue( iterator.hasNext(), input );
			var actual = iterator.next();
			assertEquals( expected.name, actual.getName(), input );
			assertEquals( expected.kind, actual.getKind(), input );
			assertEquals( expected.iterable, actual.isInIterable(), input );
			assertEquals( expected.index, actual.getIndex(), input );
			assertEquals( expected.key, actual.getKey(), input );
		}
		assertFalse( iterator.hasNext(), input );
		assertEquals( normalized, path.toString(), input );
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

	public static Object[][] malformedPaths() {
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

	@ParameterizedTest
	@MethodSource("malformedPaths")
	public void malformedPathRejected(String input) {
		assertThatThrownBy( () -> MutablePath.createPathFromString( input ) ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void rootAndNullPaths() {
		var root = MutablePath.createPathFromString( "" );
		assertEquals( "", root.toString() );
		var iterator = root.iterator();
		assertNull( iterator.next().getName() );
		assertFalse( iterator.hasNext() );
		assertThatThrownBy( () -> MutablePath.createPathFromString( null ) ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void mixedSelectorsPreserveEachNode() {
		var path = MutablePath.createPathFromString( "orders[2].addresses[home].lines[].text" );
		var iterator = path.iterator();
		assertEquals( "orders", iterator.next().getName() );
		var address = iterator.next();
		assertEquals( "addresses", address.getName() );
		assertEquals( Integer.valueOf( 2 ), address.getIndex() );
		var line = iterator.next();
		assertEquals( "lines", line.getName() );
		assertEquals( "home", line.getKey() );
		var text = iterator.next();
		assertEquals( "text", text.getName() );
		assertTrue( text.isInIterable() );
		assertNull( text.getIndex() );
		assertNull( text.getKey() );
		assertFalse( iterator.hasNext() );
		assertEquals( "orders[2].addresses[home].lines[].text", path.toString() );
	}

	@Test
	public void deeplyNestedPath() {
		var path = MutablePath.createPathFromString( "node.".repeat( 5000 ) + "leaf[0]" );
		int count = 0;
		for ( var node : path ) {
			assertEquals( count < 5000 ? "node" : count == 5000 ? "leaf" : null, node.getName() );
			count++;
		}
		assertEquals( 5002, count );
	}
}
