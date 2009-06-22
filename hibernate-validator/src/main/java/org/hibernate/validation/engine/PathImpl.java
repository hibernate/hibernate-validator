// $Id:$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.validation.Path;

/**
 * @author Hardy Ferentschik
 */
public class PathImpl implements Path {

	public static final String PROPERTY_PATH_SEPERATOR = ".";

	private final List<Node> nodeList;

	public PathImpl() {
		nodeList = new ArrayList<Node>();
		Node root = new NodeImpl( null );
		nodeList.add( root );
	}

	public PathImpl(PathImpl path) {
		this();
		Iterator<Node> iter = path.iterator();
		while(iter.hasNext()) {
			Node node = iter.next();
			nodeList.add(node);
		}
	}

	public PathImpl(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

	public Path getParentPath() {
		List<Node> nodes = new ArrayList<Node>( nodeList );
		if ( nodes.size() > 1 ) {
			nodes.remove( nodes.size() - 1 );
		}
		return new PathImpl( nodes );
	}

	public void addNode(Node node) {
		nodeList.add( node );
	}

	public Node removeLast() {
		if ( nodeList.size() < 1 ) {
			throw new IllegalStateException();
		}
		return nodeList.remove( nodeList.size() - 1 );
	}

	public Node getLast() {
		if ( nodeList.size() < 1 ) {
			throw new IllegalStateException();
		}
		return nodeList.get( nodeList.size() - 1 );
	}

	public Iterator<Path.Node> iterator() {
		return nodeList.iterator();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<Path.Node> iter = iterator();
		while ( iter.hasNext() ) {
			Node node = iter.next();
			if ( node.getName() != null ) {
				builder.append( node.getName() );

				if ( node.isInIterable() ) {
					builder.append( "[" );
					if ( node.getIndex() != null ) {
						builder.append( node.getIndex() );
					}
					else if ( node.getKey() != null ) {
						builder.append( node.getKey() );
					}
					builder.append( "]" );
				}
				if ( iter.hasNext() ) {
					builder.append( PROPERTY_PATH_SEPERATOR );

				}
			}
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PathImpl path = ( PathImpl ) o;

		if ( nodeList != null ? !nodeList.equals( path.nodeList ) : path.nodeList != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return nodeList != null ? nodeList.hashCode() : 0;
	}
}
