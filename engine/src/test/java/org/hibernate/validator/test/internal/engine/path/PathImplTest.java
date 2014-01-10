/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.engine.path;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
		assertEquals( elem.getIndex(), new Integer( 3 ) );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), "addressline" );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), null );
		assertTrue( elem.isInIterable() );
		assertEquals( elem.getIndex(), new Integer( 1 ) );

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
	public void testNonStringMapKey() {
		Validator validator = ValidatorUtil.getValidator();
		Container container = new Container();
		Key id = new Key();
		container.addItem( id, new Item( null ) );
		Set<ConstraintViolation<Container>> constraintViolations = validator.validate( container );
		assertNumberOfViolations( constraintViolations, 1 );
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
		ExecutableElement executable = ExecutableElement.forMethod(
				Container.class.getMethod(
						"addItem",
						Key.class,
						Item.class
				)
		);
		BeanMetaDataManager beanMetaDataManager = new BeanMetaDataManager(
				new ConstraintHelper(),
				new ExecutableHelper( new TypeResolutionHelper() )
		);

		ExecutableMetaData executableMetaData = beanMetaDataManager.getBeanMetaData( Container.class )
				.getMetaDataFor( executable );

		PathImpl methodParameterPath = PathImpl.createPathForExecutable( executableMetaData );

		assertEquals( methodParameterPath.toString(), "addItem" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCreationOfExecutablePathFailsDueToMissingExecutable() throws Exception {
		PathImpl.createPathForExecutable( null );
	}

	class Container {
		@Valid
		Map<Key, Item> store = new HashMap<Key, Item>();

		public void addItem(Key id, Item item) {
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
