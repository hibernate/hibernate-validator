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
package org.hibernate.validation.engine;

import java.util.ArrayList;
import java.util.List;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextImpl implements ConstraintValidatorContext {

	private final List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>( 3 );
	private final String property;
	private final String propertyParent;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;


	public ConstraintValidatorContextImpl(String propertyParent, String property, ConstraintDescriptor<?> constraintDescriptor) {
		this.property = property;
		this.propertyParent = propertyParent;
		this.constraintDescriptor = constraintDescriptor;
	}

	public void disableDefaultError() {
		defaultDisabled = true;
	}

	public String getDefaultErrorMessage() {
		return ( String ) constraintDescriptor.getAttributes().get( "message" );
	}

	public void addError(String message) {
		errorMessages.add( new ErrorMessage( message, buildPropertyPath( propertyParent, property ) ) );
	}

	public void addError(String message, String property) {
		errorMessages.add( new ErrorMessage( message, buildPropertyPath( propertyParent, property ) ) );
	}

	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	public boolean isDefaultErrorDisabled() {
		return defaultDisabled;
	}

	public List<ErrorMessage> getErrorMessages() {
		List<ErrorMessage> returnedErrorMessages = new ArrayList<ErrorMessage>( errorMessages );
		if ( !defaultDisabled ) {
			returnedErrorMessages.add(
					new ErrorMessage( getDefaultErrorMessage(), buildPropertyPath( propertyParent, property ) )
			);
		}
		return returnedErrorMessages;
	}

	private String buildPropertyPath(String parent, String leaf) {
		if ( ExecutionContext.PROPERTY_ROOT.equals( parent ) ) {
			return leaf;
		}
		else {
			return new StringBuilder().append( parent )
					.append( ExecutionContext.PROPERTY_PATH_SEPERATOR )
					.append( leaf )
					.toString();
		}
	}

	public class ErrorMessage {
		private final String message;
		private final String property;

		public ErrorMessage(String message, String property) {
			this.message = message;
			this.property = property;
		}

		public String getMessage() {
			return message;
		}

		public String getProperty() {
			return property;
		}
	}
}
