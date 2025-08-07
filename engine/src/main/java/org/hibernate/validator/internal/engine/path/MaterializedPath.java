/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.path;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;

import jakarta.validation.Path;

final class MaterializedPath implements Path, Serializable {

	@Serial
	private static final long serialVersionUID = -8906501301223202169L;

	private final NodeImpl leafNode;
	private final NodeImpl[] nodes;

	MaterializedPath(ModifiablePath path) {
		this.leafNode = path.getLeafNode();
		this.nodes = NodeImpl.constructPath( this.leafNode );
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeImpl.NodeIterator( nodes );
	}

	@Override
	public boolean equals(Object o) {
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		MaterializedPath other = (MaterializedPath) o;
		return leafNode.samePath( other.leafNode );
	}

	@Override
	public int hashCode() {
		return leafNode.hashCode();
	}

	@Override
	public String toString() {
		return ModifiablePath.asString( leafNode );
	}
}
