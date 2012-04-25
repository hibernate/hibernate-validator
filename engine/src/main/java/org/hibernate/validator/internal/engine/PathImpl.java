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
package org.hibernate.validator.internal.engine;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Path;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class PathImpl implements Path, Serializable {
	private static final long serialVersionUID = 7564511574909882392L;
	private static final Log log = LoggerFactory.make();

	public static final String PROPERTY_PATH_SEPARATOR = ".";

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
			return createNewPath( null );
		}

		return parseProperty( propertyPath );
	}

	/**
	 * Creates a path representing the specified method parameter.
	 *
	 * @param method The method hosting the parameter to represent.
	 * @param parameterName The parameter's name, e.g. "arg0" or "param1".
	 *
	 * @return A path representing the specified method parameter.
	 */
	public static PathImpl createPathForMethodParameter(Method method, String parameterName) {
		Contracts.assertNotNull( method, "A method is required to create a method parameter path." );
		Contracts.assertNotNull( parameterName, "A parameter name is required to create a method parameter path." );

		PathImpl path = createRootPath();
		path.addMethodParameterNode( method, parameterName );

		return path;
	}

	public static PathImpl createPathForMethodReturnValue(Method method) {
		Contracts.assertNotNull( method, "A method is required to create a method return value path." );

		PathImpl path = createRootPath();
		path.addMethodReturnValueNode( method );

		return path;
	}

	public static PathImpl createRootPath() {
		return createNewPath( null );
	}

	public static PathImpl createCopy(PathImpl path) {
		return new PathImpl( path );
	}

	public final boolean isRootPath() {
		return nodeList.size() == 1 && nodeList.get( 0 ).getName() == null;
	}

	public final PathImpl getPathWithoutLeafNode() {
		return new PathImpl( nodeList.subList( 0, nodeList.size() - 1 ) );
	}

	public final NodeImpl addNode(String nodeName) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = new NodeImpl( nodeName, parent, false, null, null );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	private NodeImpl addMethodParameterNode(Method method, String parameterName) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = new MethodParameterNodeImpl( method, parameterName, parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	private NodeImpl addMethodReturnValueNode(Method method) {
		NodeImpl parent = nodeList.isEmpty() ? null : (NodeImpl) nodeList.get( nodeList.size() - 1 );
		currentLeafNode = new MethodReturnValueNodeImpl( method, parent );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public final NodeImpl makeLeafNodeIterable() {
		currentLeafNode = new NodeImpl( currentLeafNode.getName(), currentLeafNode.getParent(), true, null, null );
		nodeList.remove( nodeList.size() -1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public final NodeImpl setLeafNodeIndex(Integer index) {
		currentLeafNode = new NodeImpl( currentLeafNode.getName(), currentLeafNode.getParent(), true, index, null );
		nodeList.remove( nodeList.size() -1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public final NodeImpl setLeafNodeMapKey(Object key) {
		currentLeafNode = new NodeImpl( currentLeafNode.getName(), currentLeafNode.getParent(), true, null, key );
		nodeList.remove( nodeList.size() -1 );
		nodeList.add( currentLeafNode );
		hashCode = -1;
		return currentLeafNode;
	}

	public final NodeImpl getLeafNode() {
		return currentLeafNode;
	}

	public final Iterator<Path.Node> iterator() {
		if ( nodeList.size() == 0 ) {
			return Collections.<Path.Node>emptyList().iterator();
		}
		if ( nodeList.size() == 1 ) {
			return nodeList.iterator();
		}
		return nodeList.subList( 1, nodeList.size() ).iterator();
	}

	public final String asString() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for ( int i = 1; i < nodeList.size(); i++ ) {
			NodeImpl nodeImpl = (NodeImpl) nodeList.get( i );
			if ( nodeImpl.getName() != null ) {
				if ( !first ) {
					builder.append( PROPERTY_PATH_SEPARATOR );
				}
				builder.append( nodeImpl.asString() );
			}

			first = false;
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return asString();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PathImpl path = (PathImpl) o;

		// testing equality using the Path API
		Iterator<Path.Node> p1Iterator = this.iterator();
		Iterator<Path.Node> p2Iterator = path.iterator();
		while ( p1Iterator.hasNext() ) {
			Path.Node p1Node = p1Iterator.next();
			if ( !p2Iterator.hasNext() ) {
				return false;
			}
			Path.Node p2Node = p2Iterator.next();

			// do the comparison on the node values
			if ( p2Node.getName() == null ) {
				if ( p1Node.getName() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getName().equals( p1Node.getName() ) ) {
				return false;
			}

			if ( p2Node.isInIterable() != p1Node.isInIterable() ) {
				return false;
			}

			if ( p2Node.getIndex() == null ) {
				if ( p1Node.getIndex() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getIndex().equals( p1Node.getIndex() ) ) {
				return false;
			}

			if ( p2Node.getKey() == null ) {
				if ( p1Node.getKey() != null ) {
					return false;
				}
			}
			else if ( !p2Node.getKey().equals( p1Node.getKey() ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	// deferred hash code building
	public int hashCode() {
		if ( hashCode == -1 ) {
			buildHashCode();
		}
		return hashCode;
	}

	// custom hashCode based on the string representation of a path. Don't rely on the hashCode of the NodeImpl
	// instances, because they are based on the default (reference based) implementation
	public void buildHashCode() {
		hashCode = toString().hashCode();
	}

	private static PathImpl createNewPath(String name) {
		PathImpl path = new PathImpl();
		path.addNode( name );
		return path;
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
		this.nodeList = new ArrayList<Node>();
		for ( int i = 0; i < nodeList.size(); i++ ) {
			this.nodeList.add( nodeList.get( i ) );
		}
	}

	private static PathImpl parseProperty(String property) {
		PathImpl path = createNewPath( null );
		String tmp = property;
		do {
			Matcher matcher = PATH_PATTERN.matcher( tmp );
			if ( matcher.matches() ) {

				String value = matcher.group( PROPERTY_NAME_GROUP );
				if ( !isValidJavaIdentifier( value ) ) {
					throw log.getInvalidJavaIdentifierException( value );
				}

				// create the node
				path.addNode( value );

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
				throw log.getUnableToParsePropertyPathException( property );
			}
		} while ( tmp != null );

		if ( path.getLeafNode().isIterable() ) {
			path.addNode( null );
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
