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

import javax.validation.Path;

/**
 * @author Hardy Ferentschik
 */
public class NodeImpl implements Path.Node {
	private final String name;
	private boolean isInIterable;
	private Integer index;
	private Object key;


	public NodeImpl(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isInIterable() {
		return isInIterable;
	}

	public void setInIterable(boolean inIterable) {
		isInIterable = inIterable;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Object getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "NodeImpl{" +
				"name='" + name + '\'' +
				", isInIterable=" + isInIterable +
				", index=" + index +
				", key=" + key +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		NodeImpl node = ( NodeImpl ) o;

		if ( isInIterable != node.isInIterable ) {
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
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + ( isInIterable ? 1 : 0 );
		result = 31 * result + ( index != null ? index.hashCode() : 0 );
		result = 31 * result + ( key != null ? key.hashCode() : 0 );
		return result;
	}
}
