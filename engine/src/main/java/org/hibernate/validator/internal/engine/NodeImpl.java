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
		this.hashCode = buildHashCode();
	}

	public final String getName() {
		return name;
	}

	public final boolean isInIterable() {
		return parent != null && parent.isIterable();
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

	public int buildHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( index == null ) ? 0 : index.hashCode() );
		result = prime * result + ( isIterable ? 1231 : 1237 );
		result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		result = prime * result + ( ( parent == null ) ? 0 : parent.hashCode() );
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
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
		NodeImpl other = (NodeImpl) obj;
		if ( index == null ) {
			if ( other.index != null ) {
				return false;
			}
		}
		else if ( !index.equals( other.index ) ) {
			return false;
		}
		if ( isIterable != other.isIterable ) {
			return false;
		}
		if ( key == null ) {
			if ( other.key != null ) {
				return false;
			}
		}
		else if ( !key.equals( other.key ) ) {
			return false;
		}
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		}
		else if ( !name.equals( other.name ) ) {
			return false;
		}
		if ( parent == null ) {
			if ( other.parent != null ) {
				return false;
			}
		}
		else if ( !parent.equals( other.parent ) ) {
			return false;
		}
		return true;
	}
}
