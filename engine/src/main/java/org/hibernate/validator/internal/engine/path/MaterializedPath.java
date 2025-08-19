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

	private static final String PROPERTY_PATH_SEPARATOR = ".";

	private final MaterializedNode leafNode;
	private final MaterializedNode[] nodes;

	MaterializedPath(MutablePath path) {
		this.nodes = MaterializedNode.constructMaterializedPath( path.getLeafNode() );
		this.leafNode = nodes[nodes.length - 1];
	}

	@Override
	public Iterator<Node> iterator() {
		return new MaterializedNode.NodeIterator( nodes );
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
		return asString( leafNode );
	}

	static String asString(MaterializedNode currentLeafNode) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		MaterializedNode current = currentLeafNode;
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
}
