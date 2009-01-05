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
package org.hibernate.tck;

/**
 * @author Hardy Ferentschik
 */
public class JSRReference implements Comparable {
	/**
	 * The JSR  section this instance references.
	 */
	String jsrSectionReference;

	/**
	 * The name of the class which references the JSR.
	 */
	String className;

	/**
	 * The method which references the JSR.
	 */
	String methodName;

	/**
	 * Optional note specified on the specification reference
	 */
	String note = "";

	JSRReference(String reference, String className, String methodName) {
		this.jsrSectionReference = reference;
		this.className = className;
		this.methodName = methodName;
	}

	public String getSourceLink() {
		StringBuilder builder = new StringBuilder();
		builder.append( "xref-test/" );
		builder.append( className.replace( '.', '/' ) );
		builder.append( ".html" );
		return builder.toString();
	}

	public String getJsrSectionReference() {
		return jsrSectionReference;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getNote() {
		return note;
	}

	public int compareTo(Object o) {
		return jsrSectionReference.compareTo( ( ( JSRReference ) o ).jsrSectionReference );
	}
}
