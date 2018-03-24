/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.internal.util.StringHelper;

/**
 * The path to a container element type.
 *
 * @author Guillaume Smet
 */
public class ContainerElementTypePath {

	private final List<Integer> nodes;

	private ContainerElementTypePath(List<Integer> nodes) {
		this.nodes = nodes;
	}

	public static ContainerElementTypePath root() {
		return new ContainerElementTypePath( new ArrayList<>() );
	}

	public static ContainerElementTypePath of(ContainerElementTypePath parentPath, Integer typeArgumentIndex) {
		List<Integer> nodes = new ArrayList<>( parentPath.nodes );
		nodes.add( typeArgumentIndex );

		return new ContainerElementTypePath( nodes );
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
		ContainerElementTypePath other = (ContainerElementTypePath) obj;
		if ( !this.nodes.equals( other.nodes ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return nodes.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( StringHelper.join( nodes, ", " ) ).append( "]" );
		return sb.toString();
	}
}
