/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.path;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.io.Serial;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;

import jakarta.validation.ElementKind;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.path.Path;

/**
 * Internal, mutable implementation of {@code jakarta.validation.Path}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class MutablePath implements Path, Serializable {
	@Serial
	private static final long serialVersionUID = 2464836778339203598L;
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String PROPERTY_PATH_SEPARATOR = ".";

	private MutableNode currentLeafNode;

	/**
	 * Returns a {@code Path} instance representing the path described by the
	 * given string. To create a root node the empty string should be passed.
	 *
	 * @param propertyPath the path as string representation.
	 * @return a {@code Path} instance representing the path described by the
	 * given string.
	 * @throws IllegalArgumentException in case {@code property == null} or
	 * {@code property} cannot be parsed.
	 */
	public static MutablePath createPathFromString(String propertyPath) {
		Contracts.assertNotNull( propertyPath, MESSAGES.propertyPathCannotBeNull() );

		if ( propertyPath.isEmpty() ) {
			return createRootPath();
		}

		return parseProperty( propertyPath );
	}

	public static MutablePath createPathForExecutable(ExecutableMetaData executable) {
		Contracts.assertNotNull( executable, "A method is required to create a method return value path." );

		MutablePath path = createRootPath();

		if ( executable.getKind() == ElementKind.CONSTRUCTOR ) {
			path.addConstructorNode( executable.getName(), executable.getParameterTypes() );
		}
		else {
			path.addMethodNode( executable.getName(), executable.getParameterTypes() );
		}

		return path;
	}

	public static MutablePath createRootPath() {
		return new MutablePath( MutableNode.ROOT_NODE );
	}

	public static MutablePath createCopy(MutablePath path) {
		return new MutablePath( path );
	}

	public static MutablePath createCopyWithoutLeafNode(MutablePath path) {
		return new MutablePath( path.currentLeafNode.getParent() );
	}

	public void addPropertyNode(String nodeName) {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createPropertyNode( nodeName, parent );
	}

	public void addContainerElementNode(String nodeName) {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createContainerElementNode( nodeName, parent );
	}

	public boolean needToAddContainerElementNode(String nodeName) {
		// If the node name is not null that would mean that we need to add it,
		//   but otherwise -- we may want to have an empty node
		//   if a current node is some iterable/multivalued element (E.g. array/list/map etc.).
		// If we don't add it -- the path would be broken and would lead to a situation
		//   where container elements would be pointing to a container element node itself
		//   resulting in various node methods like `Node#getIndex()` producing incorrect results.
		// As an additional side effect of not adding a node it might lead to the path not being correctly copied.
		return nodeName != null || currentLeafNode.isIterable();
	}

	public void addParameterNode(String nodeName, int index) {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createParameterNode( nodeName, parent, index );
	}

	public void addCrossParameterNode() {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createCrossParameterNode( parent );
	}

	public void addBeanNode() {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createBeanNode( parent );
	}

	public void addReturnValueNode() {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createReturnValue( parent );
	}

	private void addConstructorNode(String name, Class<?>[] parameterTypes) {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createConstructorNode( name, parent, parameterTypes );
	}

	private void addMethodNode(String name, Class<?>[] parameterTypes) {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createMethodNode( name, parent, parameterTypes );
	}

	public void addEmptyNode() {
		MutableNode parent = currentLeafNode;
		currentLeafNode = MutableNode.createNode( parent );
	}

	public void makeLeafNodeIterable() {
		assertLeafNodeIsNotRoot();
		currentLeafNode.makeIterable();
	}

	public void makeLeafNodeIterableAndSetIndex(Integer index) {
		assertLeafNodeIsNotRoot();
		currentLeafNode.makeIterableAndSetIndex( index );
	}

	public void makeLeafNodeIterableAndSetMapKey(Object key) {
		assertLeafNodeIsNotRoot();
		currentLeafNode.makeIterableAndSetMapKey( key );
	}

	public void setLeafNodeValueIfRequired(Object value) {
		// The value is only exposed for property and container element nodes
		if ( currentLeafNode.getKind() == ElementKind.PROPERTY || currentLeafNode.getKind() == ElementKind.CONTAINER_ELEMENT ) {
			currentLeafNode.setPropertyValue( value );
		}
	}

	public void setLeafNodeTypeParameter(Class<?> containerClass, Integer typeArgumentIndex) {
		assertLeafNodeIsNotRoot();
		currentLeafNode.setTypeParameter( containerClass, typeArgumentIndex );
	}

	private void assertLeafNodeIsNotRoot() {
		if ( currentLeafNode == MutableNode.ROOT_NODE ) {
			throw LOG.getUnableToModifyRootPathNodeException();
		}
	}

	public void removeLeafNode() {
		if ( currentLeafNode != null ) {
			currentLeafNode = currentLeafNode.getParent();
		}
	}

	@Override
	public MutableNode getLeafNode() {
		return currentLeafNode;
	}

	@Override
	public Iterator<Path.Node> iterator() {
		if ( currentLeafNode == null ) {
			return Collections.emptyIterator();
		}
		return new MutableNode.NodeIterator( MutableNode.constructPath( currentLeafNode ) );
	}

	public Path materialize() {
		return new MaterializedPath( this );
	}

	public String asString() {
		return asString( currentLeafNode );
	}

	static String asString(MutableNode currentLeafNode) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		MutableNode current = currentLeafNode;
		while ( !current.isRootPath() ) {
			String name = current.asString();
			if ( name.isEmpty() ) {
				current = current.getParent();
				// skip the node if it does not contribute to the string representation of the path, eg class level constraints
				continue;
			}

			if ( !first ) {
				builder.insert( 0, PROPERTY_PATH_SEPARATOR );
			}

			builder.insert( 0, current.asString() );
			first = false;
			current = current.getParent();
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return asString();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException(
				"equals() should not be called. This mutable path is for internal use only and there should be a single instance of it per validation request, hence it should not be compared/put into a hash collection" );
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException(
				"hashCode() should not be called. This mutable path is for internal use only and there should be a single instance of it per validation request, hence it should not be compared/put into a hash collection" );
	}

	/**
	 * Copy constructor.
	 *
	 * @param path the path to make a copy of.
	 */
	private MutablePath(MutablePath path) {
		currentLeafNode = path.currentLeafNode;
	}

	private MutablePath(MutableNode currentLeafNode) {
		this.currentLeafNode = currentLeafNode;
	}

	/**
	* Parses a path without access to the bean or container types. The string representation alone
	* cannot distinguish a list/array index from a numeric map key: an index of {@code 100}, an
	* {@code Integer} map key of {@code 100}, and a {@code String} map key of {@code "100"} all appear
	* as {@code [100]}. For compatibility, selectors consisting of digits that fit in an {@code int}
	* are interpreted as indexes; other non-empty selectors are interpreted as string map keys.
	* Paths built during validation retain the actual index or map key in their node metadata.
	*/
	private static MutablePath parseProperty(String propertyName) {
		MutablePath path = createRootPath();
		int length = propertyName.length();
		int position = 0;
		while ( position < length ) {
			int start = position;
			while ( position < length && propertyName.charAt( position ) != '.' && propertyName.charAt( position ) != '[' ) {
				position++;
			}
			if ( position == start ) {
				throw LOG.getUnableToParsePropertyPathException( propertyName );
			}

			String name = propertyName.substring( start, position );
			if ( !isValidJavaIdentifier( name ) ) {
				throw LOG.getInvalidJavaIdentifierException( name );
			}
			path.addPropertyNode( name );

			if ( position < length && propertyName.charAt( position ) == '[' ) {
				int indexStart = ++position;
				int index = 0;
				boolean numeric = true;
				while ( position < length && propertyName.charAt( position ) != ']' ) {
					char character = propertyName.charAt( position );
					// Preserve the ASCII word characters accepted by the former regex for keys.
					boolean digit = character >= '0' && character <= '9';
					if ( !digit && character != '_' && !( character >= 'a' && character <= 'z' ) && !( character >= 'A' && character <= 'Z' ) ) {
						throw LOG.getUnableToParsePropertyPathException( propertyName );
					}
					if ( numeric ) {
						int value = character - '0';
						if ( !digit || index > ( Integer.MAX_VALUE - value ) / 10 ) {
							numeric = false;
						}
						else {
							index = index * 10 + value;
						}
					}
					position++;
				}
				if ( position == length ) {
					throw LOG.getUnableToParsePropertyPathException( propertyName );
				}

				if ( position == indexStart ) {
					path.makeLeafNodeIterable();
				}
				else if ( numeric ) {
					path.makeLeafNodeIterableAndSetIndex( index );
				}
				else {
					path.makeLeafNodeIterableAndSetMapKey( propertyName.substring( indexStart, position ) );
				}
				position++;
			}

			if ( position < length ) {
				if ( propertyName.charAt( position ) != '.' || ++position == length ) {
					throw LOG.getUnableToParsePropertyPathException( propertyName );
				}
			}
		}

		if ( path.getLeafNode().isIterable() ) {
			path.addBeanNode();
		}
		return path;
	}

	/**
	 * Validate that the given identifier is a valid Java identifier according to the Java Language Specification,
	 * <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8">chapter 3.8</a>
	 *
	 * @param identifier string identifier to validate
	 * @return true if the given identifier is a valid Java Identifier
	 * @throws IllegalArgumentException if the given identifier is {@code null}
	 */
	private static boolean isValidJavaIdentifier(String identifier) {
		Contracts.assertNotNull( identifier, "identifier param cannot be null" );

		if ( identifier.isEmpty() || !Character.isJavaIdentifierStart( (int) identifier.charAt( 0 ) ) ) {
			return false;
		}

		for ( int i = 1; i < identifier.length(); i++ ) {
			if ( !Character.isJavaIdentifierPart( (int) identifier.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks if this PathImpl is a subpath of <code>other</code>.
	 *
	 * @param other the path to compare with
	 * @return true, if this path is a subpath
	 */
	@Deprecated(forRemoval = true, since = "9.1")
	public boolean isSubPathOf(MutablePath other) {
		if ( currentLeafNode == null ) {
			return other.currentLeafNode == null;
		}
		return currentLeafNode.isSubPathOf( other.currentLeafNode );
	}

	@Deprecated(forRemoval = true, since = "9.1")
	public boolean isSubPathOrContains(MutablePath other) {
		if ( currentLeafNode == null ) {
			return other.currentLeafNode == null;
		}
		return currentLeafNode.isSubPathOrContains( other.currentLeafNode );
	}
}
