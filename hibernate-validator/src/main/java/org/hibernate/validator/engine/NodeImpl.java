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
package org.hibernate.validator.engine;

import java.io.Serializable;
import javax.validation.Path;

/**
 * Immutable implementation of a {@code Path.Node}.
 *
 * @author Hardy Ferentschik
 */
public class NodeImpl implements Path.Node, Serializable {
	private static final long serialVersionUID = 2075466571633860499L;

	public static final String INDEX_OPEN = "[";
	public static final String INDEX_CLOSE = "]";

	private final String name;
	private final NodeImpl parent;
	private final boolean isIterable;
	private final Integer index;
	private final Object key;
	private final int hashCode;
	private String asString;

	public NodeImpl(String name, NodeImpl parent, boolean indexable, Integer index, Object key) {
		this.name = name;
		this.parent = parent;
		this.index = index;
		this.key = key;
		this.isIterable = indexable;

		// implementation is immutable. pre-calculating toString and hashCode
		this.hashCode = buildHashCode();
	}

	NodeImpl(NodeImpl node, NodeImpl parent) {
		this( node.name, parent, node.isIterable, node.index, node.key );
	}

	public final String getName() {
		return name;
	}

	public final boolean isInIterable() {
		if ( parent == null ) {
			return false;
		}
		else {
			return parent.isIterable();
		}
	}

	public final boolean isIterable() {
		return isIterable;
	}

	public final Integer getIndex() {
		if ( parent == null ) {
			return null;
		}
		else {
			return parent.index;
		}
	}

	public final Object getKey() {
		if ( parent == null ) {
			return null;
		}
		else {
			return parent.key;
		}
	}

	public final NodeImpl getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return asString();
	}

	public final String asString() {
		if ( asString == null ) {
			asString = buildToString();
		}
		return asString;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		NodeImpl node = (NodeImpl) o;

		if ( isIterable != node.isIterable ) {
			return false;
		}
		if ( index != null ? !index.equals( node.index ) : node.index != null ) {
			return false;
		}
		if ( key != null ? !key.equals( node.key ) : node.key != null ) {
			return false;
		}
		if ( name != null ? !name.equals( node.name ) : node.name != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int buildHashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + ( isIterable ? 1 : 0 );
		result = 31 * result + ( index != null ? index.hashCode() : 0 );
		result = 31 * result + ( key != null ? key.hashCode() : 0 );
		return result;
	}

	private String buildToString() {
		StringBuilder builder = new StringBuilder();
		builder.append( getName() );
		if ( isIterable() ) {
			builder.append( INDEX_OPEN );
			if ( index != null ) {
				builder.append( index );
			}
			else if ( key != null ) {
				builder.append( key );
			}
			builder.append( INDEX_CLOSE );
		}
		return builder.toString();
	}
}
