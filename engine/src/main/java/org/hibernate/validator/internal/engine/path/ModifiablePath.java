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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ElementKind;
import jakarta.validation.Path;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Default implementation of {@code jakarta.validation.Path}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class ModifiablePath implements Path, Serializable {
	@Serial
	private static final long serialVersionUID = 7564511574909882392L;
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String PROPERTY_PATH_SEPARATOR = ".";

	/**
	 * Regular expression used to split a string path into its elements.
	 *
	 * @see <a href="http://www.regexplanet.com/simple/index.jsp">Regular expression tester</a>
	 */
	private static final String LEADING_PROPERTY_GROUP = "[^\\[\\.]++"; // everything up to a [ or .
	private static final String OPTIONAL_INDEX_GROUP = "\\[(\\w*+)\\]";
	private static final String REMAINING_PROPERTY_STRING = "\\.(.++)"; // processed recursively

	private static final Pattern PATH_PATTERN = Pattern.compile( "(" + LEADING_PROPERTY_GROUP + ")(" + OPTIONAL_INDEX_GROUP + ")?(" + REMAINING_PROPERTY_STRING + ")*+" );
	private static final int PROPERTY_NAME_GROUP = 1;
	private static final int INDEXED_GROUP = 2;
	private static final int INDEX_GROUP = 3;
	private static final int REMAINING_STRING_GROUP = 5;

	private NodeImpl currentLeafNode;
	private int hashCode;

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
	public static ModifiablePath createPathFromString(String propertyPath) {
		Contracts.assertNotNull( propertyPath, MESSAGES.propertyPathCannotBeNull() );

		if ( propertyPath.isEmpty() ) {
			return createRootPath();
		}

		return parseProperty( propertyPath );
	}

	public static ModifiablePath createPathForExecutable(ExecutableMetaData executable) {
		Contracts.assertNotNull( executable, "A method is required to create a method return value path." );

		ModifiablePath path = createRootPath();

		if ( executable.getKind() == ElementKind.CONSTRUCTOR ) {
			path.addConstructorNode( executable.getName(), executable.getParameterTypes() );
		}
		else {
			path.addMethodNode( executable.getName(), executable.getParameterTypes() );
		}

		return path;
	}

	public static ModifiablePath createRootPath() {
		return new ModifiablePath( NodeImpl.ROOT_NODE );
	}

	public static ModifiablePath createCopy(ModifiablePath path) {
		return new ModifiablePath( path );
	}

	public static ModifiablePath createCopyWithoutLeafNode(ModifiablePath path) {
		return new ModifiablePath( path.currentLeafNode.getParent() );
	}

	public boolean isRootPath() {
		return currentLeafNode.isRootPath();
	}

	public NodeImpl addPropertyNode(String nodeName) {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createPropertyNode( nodeName, parent );
		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl addContainerElementNode(String nodeName) {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createContainerElementNode( nodeName, parent );
		resetHashCode();
		return currentLeafNode;
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

	public NodeImpl addParameterNode(String nodeName, int index) {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createParameterNode( nodeName, parent, index );
		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl addCrossParameterNode() {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createCrossParameterNode( parent );
		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl addBeanNode() {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createBeanNode( parent );
		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl addReturnValueNode() {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createReturnValue( parent );
		resetHashCode();
		return currentLeafNode;
	}

	private NodeImpl addConstructorNode(String name, Class<?>[] parameterTypes) {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createConstructorNode( name, parent, parameterTypes );
		resetHashCode();
		return currentLeafNode;
	}

	private NodeImpl addMethodNode(String name, Class<?>[] parameterTypes) {
		NodeImpl parent = currentLeafNode;
		currentLeafNode = NodeImpl.createMethodNode( name, parent, parameterTypes );
		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl makeLeafNodeIterable() {
		currentLeafNode = NodeImpl.makeIterable( currentLeafNode );

		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl makeLeafNodeIterableAndSetIndex(Integer index) {
		currentLeafNode = NodeImpl.makeIterableAndSetIndex( currentLeafNode, index );

		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl makeLeafNodeIterableAndSetMapKey(Object key) {
		currentLeafNode = NodeImpl.makeIterableAndSetMapKey( currentLeafNode, key );

		resetHashCode();
		return currentLeafNode;
	}

	public NodeImpl setLeafNodeValueIfRequired(Object value) {
		// The value is only exposed for property and container element nodes
		if ( currentLeafNode.getKind() == ElementKind.PROPERTY || currentLeafNode.getKind() == ElementKind.CONTAINER_ELEMENT ) {
			currentLeafNode = NodeImpl.setPropertyValue( currentLeafNode, value );

			// the property value is not part of the NodeImpl hashCode so we don't need to reset the PathImpl hashCode
		}
		return currentLeafNode;
	}

	public NodeImpl setLeafNodeTypeParameter(Class<?> containerClass, Integer typeArgumentIndex) {
		currentLeafNode = NodeImpl.setTypeParameter( currentLeafNode, containerClass, typeArgumentIndex );

		resetHashCode();
		return currentLeafNode;
	}

	public void removeLeafNode() {
		if ( currentLeafNode != null ) {
			currentLeafNode = currentLeafNode.getParent();
			resetHashCode();
		}
	}

	public NodeImpl getLeafNode() {
		return currentLeafNode;
	}

	@Override
	public Iterator<Path.Node> iterator() {
		if ( currentLeafNode == null ) {
			return Collections.emptyIterator();
		}
		return new NodeImpl.NodeIterator( NodeImpl.constructPath( currentLeafNode ) );
	}

	public Path materialize() {
		return new MaterializedPath( this );
	}

	public String asString() {
		return asString( currentLeafNode );
	}

	static String asString(NodeImpl currentLeafNode) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		NodeImpl current = currentLeafNode;
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
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ModifiablePath other = (ModifiablePath) obj;
		if ( currentLeafNode == null ) {
			if ( other.currentLeafNode != null ) {
				return false;
			}
		}
		else if ( !currentLeafNode.samePath( other.currentLeafNode ) ) {
			return false;
		}
		return true;
	}

	@Override
	// deferred hash code building
	public int hashCode() {
		if ( hashCode == -1 ) {
			hashCode = buildHashCode();
		}

		return hashCode;
	}

	private int buildHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ( ( currentLeafNode == null ) ? 0 : currentLeafNode.hashCode() );
		return result;
	}

	/**
	 * Copy constructor.
	 *
	 * @param path the path to make a copy of.
	 */
	private ModifiablePath(ModifiablePath path) {
		currentLeafNode = path.currentLeafNode;
		hashCode = path.hashCode;
	}

	private ModifiablePath() {
		hashCode = -1;
	}

	private ModifiablePath(NodeImpl currentLeafNode) {
		this.currentLeafNode = currentLeafNode;
		hashCode = -1;
	}

	private void resetHashCode() {
		hashCode = -1;
	}

	private static ModifiablePath parseProperty(String propertyName) {
		ModifiablePath path = createRootPath();
		String tmp = propertyName;
		do {
			Matcher matcher = PATH_PATTERN.matcher( tmp );
			if ( matcher.matches() ) {

				String value = matcher.group( PROPERTY_NAME_GROUP );
				if ( !isValidJavaIdentifier( value ) ) {
					throw LOG.getInvalidJavaIdentifierException( value );
				}

				// create the node
				path.addPropertyNode( value );

				// is the node indexable
				if ( matcher.group( INDEXED_GROUP ) != null ) {
					path.makeLeafNodeIterable();
				}

				// take care of the index/key if one exists
				String indexOrKey = matcher.group( INDEX_GROUP );
				if ( indexOrKey != null && indexOrKey.length() > 0 ) {
					try {
						Integer i = Integer.parseInt( indexOrKey );
						path.makeLeafNodeIterableAndSetIndex( i );
					}
					catch (NumberFormatException e) {
						path.makeLeafNodeIterableAndSetMapKey( indexOrKey );
					}
				}

				// match the remaining string
				tmp = matcher.group( REMAINING_STRING_GROUP );
			}
			else {
				throw LOG.getUnableToParsePropertyPathException( propertyName );
			}
		} while ( tmp != null );

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

		if ( identifier.length() == 0 || !Character.isJavaIdentifierStart( (int) identifier.charAt( 0 ) ) ) {
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
	public boolean isSubPathOf(ModifiablePath other) {
		if ( currentLeafNode == null ) {
			return other.currentLeafNode == null;
		}
		return currentLeafNode.isSubPathOf( other.currentLeafNode );
	}

	public boolean isSubPathOrContains(ModifiablePath other) {
		if ( currentLeafNode == null ) {
			return other.currentLeafNode == null;
		}
		return currentLeafNode.isSubPathOrContains( other.currentLeafNode );
	}
}
