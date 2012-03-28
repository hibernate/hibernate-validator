/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.constraintvalidators;

/**
 * @author Hardy Ferentschik
 */
public class MyCustomStringImpl implements CharSequence {
	private final String myString;

	MyCustomStringImpl(String s) {
		this.myString = s;
	}

	public int length() {
		return myString.length();
	}

	public char charAt(int i) {
		return myString.charAt( i );
	}

	public CharSequence subSequence(int i, int j) {
		return myString.subSequence( i, j );
	}

	public String toString() {
		return myString;
	}
}
