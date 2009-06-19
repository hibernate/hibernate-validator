// $Id$
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper class to iterate over a property path.
 *
 * @author Hardy Ferentschik
 */
public class PropertyPath implements Iterable<PropertyPath.PathElement> {

	/**
	 * Regular expression used to split the path into its elements.
	 *
	 * @see <a href="http://www.regexplanet.com/simple/index.jsp">Regular expression tester</a>
	 */
	private static final Pattern pathPattern = Pattern.compile( "(\\w+)(\\[(\\w+)\\])?(\\.(.*))*" );

	final String originalProperty;

	final List<PathElement> pathList = new ArrayList<PathElement>();

	/**
	 * Constructs a new {@code PropertyPath}.
	 *
	 * @param property The string representation of the property path.
	 *
	 * @throws IllegalArgumentException in case {@code property == null} or {@code property} cannot be parsed.
	 */
	public PropertyPath(String property) {
		if ( property == null ) {
			throw new IllegalArgumentException( "null is not allowed as property path." );
		}

		this.originalProperty = property;
		if ( property.length() > 0 ) {
			parseProperty( property );
		}
	}

	private void parseProperty(String property) {
		String tmp = property;
		do {
			Matcher matcher = pathPattern.matcher( tmp );
			if ( matcher.matches() ) {
				String value = matcher.group( 1 );
				String index = matcher.group( 3 );
				PathElement elem = new PathElement( value, index );
				pathList.add( elem );
				tmp = matcher.group( 5 );
			}
			else {
				throw new IllegalArgumentException( "Unable to parse property path " + property );
			}
		} while ( tmp != null );
	}

	public String getOriginalProperty() {
		return originalProperty;
	}

	public Iterator<PathElement> iterator() {
		return pathList.iterator();
	}

	@Override
	public String toString() {
		return "PropertyPath{" +
				"originalProperty='" + originalProperty + '\'' +
				", pathList=" + pathList +
				'}';
	}

	public static class PathElement {
		private final String value;
		private final String index;

		private PathElement(String value, String index) {
			this.value = value;
			this.index = index;
		}

		public String value() {
			return value;
		}

		public String getIndex() {
			return index;
		}

		public boolean isIndexed() {
			return index != null;
		}

		@Override
		public String toString() {
			return "PathElement{" +
					"value='" + value + '\'' +
					", index='" + index + '\'' +
					'}';
		}
	}
}