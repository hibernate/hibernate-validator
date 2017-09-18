/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.path;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.validation.Path;

import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Gunnar Morling
 *
 */
public class PathImpl implements Path, Serializable {

	private final List<Node> nodeList;
	private final int hashCode;

	public PathImpl(List<Node> nodeList) {
		this.nodeList = CollectionHelper.toImmutableList( nodeList );
		this.hashCode = buildHashCode();
	}

	@Override
	public Iterator<Node> iterator() {
		if ( nodeList.size() == 0 ) {
			return Collections.<Path.Node>emptyList().iterator();
		}
		if ( nodeList.size() == 1 ) {
			return nodeList.iterator();
		}
		return nodeList.subList( 1, nodeList.size() ).iterator();
	}

	public boolean isRootPath() {
		return nodeList.size() == 1 && nodeList.get( 0 ).getName() == null;
	}

	@Override
	public String toString() {
		return asString();
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
				builder.append( PathBuilder.PROPERTY_PATH_SEPARATOR );
			}

			builder.append( nodeImpl.asString() );

			first = false;
		}
		return builder.toString();
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

		return nodeList.equals( other.nodeList );
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int buildHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ( ( nodeList == null ) ? 0 : nodeList.hashCode() );
		return result;
	}
}
