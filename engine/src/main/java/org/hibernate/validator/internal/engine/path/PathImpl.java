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
package org.hibernate.validator.internal.engine.path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ElementKind;
import javax.validation.Path;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Default implementation of {@code javax.validation.Path}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class PathImpl implements Path, Serializable {
	private static final long serialVersionUID = 7564511574909882392L;
	private static final Log log = LoggerFactory.make();

	private static final String PROPERTY_PATH_SEPARATOR = ".";

	/**
	 * Regular expression used to split a string path into its elements.
	 *
	 * @see <a href="http://www.regexplanet.com/simple/index.jsp">Regular expression tester</a>
	 */
	private static final String LEADING_PROPERTY_GROUP = "[^\\[\\.]+";  // everything up to a [ or .
	private static final String OPTIONAL_INDEX_GROUP = "\\[(\\w*)\\]";
	private static final String REMAINING_PROPERTY_STRING = "\\.(.*)";  // processed recursively

	private static final Pattern PATH_PATTERN = Pattern.compile( "(" + LEADING_PROPERTY_GROUP + ")(" + OPTIONAL_INDEX_GROUP + ")?(" + REMAINING_PROPERTY_STRING + ")*" );
	private static final int PROPERTY_NAME_GROUP = 1;
	private static final int INDEXED_GROUP = 2;
	private static final int INDEX_GROUP = 3;
	private static final int REMAINING_STRING_GROUP = 5;

	private final List<Node> nodeList;
	private NodeImpl currentLeafNode;
	private int hashCode;

	/**
	 * Returns a {@code Path} instance representing the path described by the
	 * given string. To create a root node the empty string should be passed.
	 *
	 * @param propertyPath the path as string representation.
	 *
	 * @return a {@code Path} instance representing the path described by the
	 *         given string.
	 *
	 * @throws IllegalArgumentException in case {@code property == null} or
	 * {@code property} cannot be parsed.
	 */
	public static PathImpl createPathFromString(String propertyPath) {
		Contracts.assertNotNull( propertyPath, MESSAGES.propertyPathCannotBeNull() );

		if ( propertyPath.length() == 0 ) {
			return createRootPath();
		}

		return parseProperty( propertyPath );
	}

	public static PathImpl createPathForExecutable(ExecutableMetaData executable) {
		Contracts.assertNotNull( executable, "A method is required to create a method return value path." );

		PathImpl path = createRootPath();

		if ( executable.getKind() == ElementKind.CONSTRUCTOR ) {
			path.addConstructorNode( executable.getName(), Arrays.asList( executable.getParameterTypes() ) );
		}
		else {
			path.addMethodNode( executable.getName(), Arrays.asList( executable.getParameterTypes() ) );
		}

		return path;
	}

	public static PathImpl createRootPath() {
		PathImpl path = new PathImpl();
		path.addBeanNode();
		return path;
	}

	public static PathImpl createCopy(PathImpl path) {
		return new PathImpl( path );
	}

	public boolean isRootPath() {
		return nodeList.size() == 1 && nodeList.get( 0 ).getName() == null;
	}

	public PathImpl getPathWithoutLeafNode() {
		return new PathImpl( nodeList.subList( 0, nodeList.size() - 1 ) );
	}

	public NodeImpl addPropertyNode(String nodeName) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createPropertyNode( nodeName, parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl addParameterNode(String nodeName, int index) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createParameterNode( nodeName, parent, index );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl addCrossParameterNode() {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createCrossParameterNode( parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl addBeanNode() {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createBeanNode( parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl addReturnValueNode() {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createReturnValue( parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	private NodeImpl addConstructorNode(String name, List<Class<?>> parameterTypes) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createConstructorNode( name, parent, parameterTypes );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	private NodeImpl addMethodNode(String name, List<Class<?>> parameterTypes) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = NodeImpl.createMethodNode( name, parent, parameterTypes );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl makeLeafNodeIterable() {
		currentLeafNode = NodeImpl.makeIterable( currentLeafNode );

		nodeList.remove( nodeList.size() - 1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl setLeafNodeIndex(Integer index) {
		currentLeafNode = NodeImpl.setIndex( currentLeafNode, index );

		nodeList.remove( nodeList.size() - 1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl setLeafNodeMapKey(Object key) {
		currentLeafNode = NodeImpl.setMapKey( currentLeafNode, key );

		nodeList.remove( nodeList.size() - 1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public NodeImpl getLeafNode() {
		return currentLeafNode;
	}

	@Override
	public Iterator<Path.Node> iterator() {
		if ( nodeList.size() == 0 ) {
			return Collections.<Path.Node>emptyList().iterator();
		}
		if ( nodeList.size() == 1 ) {
			return nodeList.iterator();
		}
		return nodeList.subList( 1, nodeList.size() ).iterator();
	}

	public String asString() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for ( int i = 1; i < nodeList.size(); i++ ) {
			NodeImpl nodeImpl = (NodeImpl) nodeList.get( i );
			String name = nodeImpl.asString();
			if ( name.isEmpty() ) {
				// skip the node if it does not contribute to the string representation of the path, eg class level constraints
				continue;
			}

			if ( !first ) {
				builder.append( PROPERTY_PATH_SEPARATOR );
			}

			builder.append( nodeImpl.asString() );

			first = false;
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
		PathImpl other = (PathImpl) obj;
		if ( nodeList == null ) {
			if ( other.nodeList != null ) {
				return false;
			}
		}
		else if ( !nodeList.equals( other.nodeList ) ) {
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
				+ ( ( nodeList == null ) ? 0 : nodeList.hashCode() );
		return result;
	}

	/**
	 * Copy constructor.
	 *
	 * @param path the path to make a copy of.
	 */
	private PathImpl(PathImpl path) {
		this( path.nodeList );
		currentLeafNode = (NodeImpl) nodeList.get( nodeList.size() - 1 );
	}

	private PathImpl() {
		nodeList = new ArrayList<Node>();
	}

	private PathImpl(List<Node> nodeList) {
		this.nodeList = new ArrayList<Node>( nodeList );
	}

	private static PathImpl parseProperty(String propertyName) {
		PathImpl path = createRootPath();
		String tmp = propertyName;
		do {
			Matcher matcher = PATH_PATTERN.matcher( tmp );
			if ( matcher.matches() ) {

				String value = matcher.group( PROPERTY_NAME_GROUP );
				if ( !isValidJavaIdentifier( value ) ) {
					throw log.getInvalidJavaIdentifierException( value );
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
						path.setLeafNodeIndex( i );
					}
					catch ( NumberFormatException e ) {
						path.setLeafNodeMapKey( indexOrKey );
					}
				}

				// match the remaining string
				tmp = matcher.group( REMAINING_STRING_GROUP );
			}
			else {
				throw log.getUnableToParsePropertyPathException( propertyName );
			}
		} while ( tmp != null );

		if ( path.getLeafNode().isIterable() ) {
			path.addBeanNode();
		}

		return path;
	}

	/**
	 * Validate that the given identifier is a valid Java identifier according to the Java Language Specification,
	 * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.8">chapter 3.8</a>
	 *
	 * @param identifier string identifier to validate
	 *
	 * @return true if the given identifier is a valid Java Identifier
	 *
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
}
