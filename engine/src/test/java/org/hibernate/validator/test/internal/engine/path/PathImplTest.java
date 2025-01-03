/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.path;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManagerImpl;
import org.hibernate.validator.internal.metadata.DefaultBeanMetaDataClassNormalizer;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class PathImplTest {

	@Test
	public void testParsing() {
		String property = "orders[3].deliveryAddress.addressline[1]";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( elem.getName(), "orders" );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), "deliveryAddress" );
		assertTrue( elem.isInIterable() );
		assertEquals( elem.getIndex(), Integer.valueOf( 3 ) );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), "addressline" );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), null );
		assertTrue( elem.isInIterable() );
		assertEquals( elem.getIndex(), Integer.valueOf( 1 ) );

		assertFalse( propIter.hasNext() );

		assertEquals( path.toString(), property );
	}

	@Test
	public void testParsingPropertyWithCurrencySymbol() {
		PathImpl path = PathImpl.createPathFromString( "€Amount" );
		Iterator<Path.Node> it = path.iterator();

		assertEquals( it.next().getName(), "€Amount" );
	}

	@Test
	public void testParsingPropertyWithGermanCharacter() {
		PathImpl path = PathImpl.createPathFromString( "höchstBetrag" );
		Iterator<Path.Node> it = path.iterator();

		assertEquals( it.next().getName(), "höchstBetrag" );
	}

	@Test
	public void testParsingPropertyWithUnicodeCharacter() {
		PathImpl path = PathImpl.createPathFromString( "höchst\u00f6Betrag" );
		Iterator<Path.Node> it = path.iterator();

		assertEquals( it.next().getName(), "höchst\u00f6Betrag" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testParsingInvalidJavaProperty() {
		PathImpl.createPathFromString( "1invalid" );
	}

	@Test
	public void testParseMapBasedProperty() {
		String property = "order[foo].deliveryAddress";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( "order", elem.getName() );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "deliveryAddress", elem.getName() );
		assertTrue( elem.isInIterable() );
		assertEquals( "foo", elem.getKey() );

		assertFalse( propIter.hasNext() );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNull() {
		PathImpl.createPathFromString( null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testUnbalancedBraces() {
		PathImpl.createPathFromString( "foo[.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIndexInMiddleOfProperty() {
		PathImpl.createPathFromString( "f[1]oo.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTrailingPathSeparator() {
		PathImpl.createPathFromString( "foo.bar." );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testLeadingPathSeparator() {
		PathImpl.createPathFromString( ".foo.bar" );
	}

	@Test
	public void testEmptyString() {
		Path path = PathImpl.createPathFromString( "" );
		assertTrue( path.iterator().hasNext() );
	}

	@Test
	public void testIsSubPathOf() {
		PathImpl subPath = PathImpl.createPathFromString( "annotation" );
		PathImpl middlePath = PathImpl.createPathFromString( "annotation.property" );
		PathImpl middlePath2 = PathImpl.createPathFromString( "annotation.property[2]" );
		PathImpl middlePath3 = PathImpl.createPathFromString( "annotation.property[3]" );
		PathImpl fullPath3 = PathImpl.createPathFromString( "annotation.property[3].element" );
		PathImpl fullPath4 = PathImpl.createPathFromString( "annotation.property[4].element" );

		assertTrue( subPath.isSubPathOf( middlePath ), "bean is subpath of its properties" );
		assertFalse( middlePath.isSubPathOf( subPath ), "a property is not a subPath of its bean" );
		assertTrue( subPath.isSubPathOf( fullPath3 ), "bean is subpath of its tree" );
		assertTrue( subPath.isSubPathOf( fullPath4 ), "bean is subpath of its tree, for every array index" );
		assertFalse( middlePath.isSubPathOf( fullPath3 ), "property is not a subpath of an array" );
		assertFalse( middlePath3.isSubPathOf( fullPath3 ), "array property is not a subpath of a property of a bean in an array" );
		assertFalse( middlePath2.isSubPathOf( fullPath3 ), "array element is not a subpath of another element's children" );
		assertFalse( fullPath3.isSubPathOf( fullPath4 ), "different array elements are not subpaths of the other" );
	}

	@Test
	public void testIsSubPathOrContains() {
		PathImpl rootPath = PathImpl.createPathFromString( "" );
		PathImpl subPath = PathImpl.createPathFromString( "annotation" );
		PathImpl middlePath = PathImpl.createPathFromString( "annotation.property" );
		PathImpl middlePath2 = PathImpl.createPathFromString( "annotation.property[2]" );
		PathImpl middlePath3 = PathImpl.createPathFromString( "annotation.property[3]" );
		PathImpl fullPath3 = PathImpl.createPathFromString( "annotation.property[3].element" );
		PathImpl fullPath4 = PathImpl.createPathFromString( "annotation.property[4].element" );

		assertTrue( rootPath.isSubPathOrContains( middlePath ), "root path is in every path" );
		assertTrue( middlePath.isSubPathOrContains( rootPath ), "every path contains the root path" );
		assertTrue( subPath.isSubPathOrContains( middlePath ), "bean is subpath of its properties" );
		assertTrue( middlePath.isSubPathOrContains( subPath ), "a property is an extension of its bean" );
		assertTrue( subPath.isSubPathOrContains( fullPath3 ), "bean is subpath of its tree" );
		assertTrue( subPath.isSubPathOrContains( fullPath4 ), "bean is subpath of its tree, for every array index" );
		assertFalse( middlePath.isSubPathOrContains( fullPath3 ), "property is not a subpath of an array" );
		assertFalse( middlePath3.isSubPathOrContains( fullPath3 ), "array property is not a subpath of a property of a bean in an array" );
		assertFalse( middlePath2.isSubPathOrContains( fullPath3 ), "array element is not a subpath of another element's children" );
		assertFalse( fullPath3.isSubPathOrContains( fullPath4 ), "different array elements are not subpaths of the other" );
	}

	@Test
	public void testNonStringMapKey() {
		Validator validator = ValidatorUtil.getValidator();
		Container container = new Container();
		Key id = new Key();
		container.addItem( id, new Item( null ) );
		Set<ConstraintViolation<Container>> constraintViolations = validator.validate( container );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "store" )
								.property( "id", true, id, null, Map.class, 1 )
						)
		);
		ConstraintViolation<Container> violation = constraintViolations.iterator().next();
		Path path = violation.getPropertyPath();
		Iterator<Path.Node> iter = path.iterator();
		iter.next();
		Path.Node node = iter.next();
		assertNotNull( node );
		assertTrue( node.isInIterable() );
		assertEquals( node.getKey(), id );
	}

	@Test
	public void testCreationOfExecutablePath() throws Exception {
		Method executable = Container.class.getMethod( "addItem", Key.class, Item.class );

		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManagerImpl(
				getDummyConstraintCreationContext(),
				new ExecutableHelper( new TypeResolutionHelper() ),
				new ExecutableParameterNameProvider( new DefaultParameterNameProvider() ),
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ),
				new DefaultBeanMetaDataClassNormalizer(),
				new ValidationOrderGenerator(),
				Collections.<MetaDataProvider>emptyList(),
				new MethodValidationConfiguration.Builder().build()
		);

		ExecutableMetaData executableMetaData = beanMetaDataManager.getBeanMetaData( Container.class )
				.getMetaDataFor( executable ).get();

		PathImpl methodParameterPath = PathImpl.createPathForExecutable( executableMetaData );

		assertEquals( methodParameterPath.toString(), "addItem" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCreationOfExecutablePathFailsDueToMissingExecutable() throws Exception {
		PathImpl.createPathForExecutable( null );
	}

	class Container {
		@Valid
		Map<Key, Item> store = new HashMap<>();

		public void addItem(@NotNull Key id, @NotNull Item item) {
			store.put( id, item );
		}
	}

	class Key {
	}

	class Item {
		@NotNull
		String id;

		Item(String id) {
			this.id = id;
		}
	}
}
