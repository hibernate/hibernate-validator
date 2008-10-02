// $Id: NotEmptyConstraint.java 110 2008-09-29 23:46:37Z epbernard $
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
package org.hibernate.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Context;

/**
 * @author Hardy Ferentschik
 * @todo Extend to not only support strings, but also collections and maps. Needs to be specified first though.
 */
public class NotEmptyConstraint implements Constraint<NotEmpty> {

	public void initialize(NotEmpty parameters) {
	}

	public boolean isValid(Object object, Context context) {
		if ( object == null ) {
			return true;
		}
		if ( !( object instanceof String ) ) {
			throw new IllegalArgumentException( "Expected String type." );
		}
		String string = ( String ) object;
		int length = string.length();
		return length > 0;
	}
}
