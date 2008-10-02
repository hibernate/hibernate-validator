// $Id: PropertyIterator.java 112 2008-09-30 08:08:50Z hardy.ferentschik $
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
package org.hibernate.validation.util;

/**
 * @author Hardy Ferentschik
 */

/**
 * Helper class to iterate over a property. After constructing an instance of this class one can with
 * successive calls to <code>split()</code> split the property into a head and tail section. The head will contain the
 * property up to the first '.' and tail the rest. If head is an indexed value it is further seperated into its actual
 * value and index. For example, <code>new PropertyNavigator("order[2].orderNumer").split()</code> will result into:
 * <ul>
 * <li> <code>getHead() == "order"</code> </li>
 * <li> <code>getIndex() == "2"</code> </li>
 * <li> <code>getTail() == "orderNumber"</code> </li>
 * </ul>.
 */
public class PropertyIterator {
	final String originalProperty;
	String head;
	String index;
	String tail;

	public PropertyIterator(String property) {
		this.originalProperty = property;
		if ("".equals( property ) ) {
			this.tail = null;
		} else {
			this.tail = property;
		}
	}

	public boolean hasNext() {
		return tail != null;
	}

	/**
	 * Splits the property at the next '.'
	 *
	 * @todo Add error handling in case the property uses wrong characters or has unbalanced []
	 */
	public void split() {

		if ( tail == null ) {
			return;
		}

		String[] tokens = tail.split( "\\.", 2 ); // split the property at the first .

		head = tokens[0];
		index = null;

		if ( head.contains( "[" ) ) {
			head = tokens[0].substring( 0, tokens[0].indexOf( "[" ) );
			index = tokens[0].substring( tokens[0].indexOf( "[" ) + 1, tokens[0].indexOf( "]" ) );
		}

		if ( tokens.length > 1 ) {
			tail = tokens[1];
		}
		else {
			tail = null;
		}
	}

	public String getOriginalProperty() {
		return originalProperty;
	}

	public String getHead() {
		return head;
	}

	public String getTail() {
		return tail;
	}

	public String getIndex() {
		return index;
	}

	public boolean isIndexed() {
		return index != null;
	}
}
