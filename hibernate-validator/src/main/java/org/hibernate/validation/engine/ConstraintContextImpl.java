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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.validation.ConstraintContext;
import javax.validation.ConstraintDescriptor;

import org.hibernate.validation.impl.ConstraintDescriptorImpl;

/**
 * @author Emmanuel Bernard
 */
public class ConstraintContextImpl implements ConstraintContext {
	private final ConstraintDescriptor constraintDescriptor;
	private final List<ErrorMessage> errorMessages;
	private boolean defaultDisabled;

	public ConstraintContextImpl(ConstraintDescriptorImpl constraintDescriptor) {
		this.constraintDescriptor = constraintDescriptor;
		this.errorMessages = new ArrayList<ErrorMessage>(3);
	}

	public void disableDefaultError() {
		defaultDisabled = true;
	}

	public String getDefaultErrorMessage() {
		return ( String ) constraintDescriptor.getParameters().get("message");
	}

	public void addError(String message) {
		//FIXME get the default property if property-level
		errorMessages.add( new ErrorMessage( message, null ) );
	}

	public void addError(String message, String property) {
		//FIXME: make sure the property is valid
		errorMessages.add( new ErrorMessage( message, property ) );
	}

	public List<ErrorMessage> getErrorMessages() {
		List<ErrorMessage> returnedErrorMessages = new ArrayList<ErrorMessage>( errorMessages.size() + 1 );
		Collections.copy( returnedErrorMessages, errorMessages );
		if ( ! defaultDisabled ) {
			//FIXME get the default property if property-level
			returnedErrorMessages.add( new ErrorMessage( getDefaultErrorMessage(), null) );
		}
		return returnedErrorMessages;
	}

	public static class ErrorMessage {
		private final String message;
		private final String property;

		private ErrorMessage(String message, String property) {
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
