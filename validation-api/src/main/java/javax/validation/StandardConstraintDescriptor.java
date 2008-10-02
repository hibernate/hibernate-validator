// $Id: StandardConstraintDescriptor.java 114 2008-10-01 13:44:26Z hardy.ferentschik $
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
package javax.validation;

/**
 * Describe how the current constraint influences the standard constraints.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class StandardConstraintDescriptor {
	/**
	 * Defines the object nullability.
	 *
	 * @return <code>TRUE<code> if the object is nullable, <code>FALSE</code> if object is not
	 *         nullable and <code>null</code> if nullabiltiy does not apply.
	 */
	public Boolean getNullability() {
		return null;
	}

	/**
	 * Defines the precision if the validated object is a number.
	 *
	 * @return the number's precision or <code>null</code> if precision does not apply.
	 */
	public Integer getPrecision() {
		return null;
	}

	/**
	 * Defines the scale if the validated object is a number.
	 *
	 * @return the number's scale or <code>null</code> if scale does not apply.
	 */
	public Integer getScale() {
		return null;
	}

	/**
	 * Defines the length if the validated object is a string.
	 *
	 * @return the strings's length or <code>null</code> if length does not apply.
	 */
	public Integer getLength() {
		return null;
	}
}
