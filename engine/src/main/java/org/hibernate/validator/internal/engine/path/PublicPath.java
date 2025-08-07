/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.path;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

import jakarta.validation.Path;

final class PublicPath implements Path, Serializable {

	@Serial
	private static final long serialVersionUID = -8906501301223202169L;

	private final NodeImpl leafNode;
	private final NodeImpl[] nodes;
	private final int hashCode;

	PublicPath(ModifiablePath path) {
		this.leafNode = path.getLeafNode();
		this.nodes = NodeImpl.constructPath( this.leafNode );
		this.hashCode = path.hashCode();
	}

	@Override
	public Iterator<Node> iterator() {
		if ( nodes.length == 0 ) {
			return Collections.emptyIterator();
		}
		return new NodeImpl.NodeIterator( nodes );
	}

	@Override
	public boolean equals(Object o) {
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		PublicPath other = (PublicPath) o;
		return leafNode.samePath( other.leafNode );
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return ModifiablePath.asString( leafNode );
	}
}
