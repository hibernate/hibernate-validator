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
import javax.validation.ConstraintValidatorContext;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextImpl implements ConstraintValidatorContext {

	private final List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>( 3 );
	private final Path propertyPath;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private boolean defaultDisabled;


	public ConstraintValidatorContextImpl(Path propertyPath, ConstraintDescriptor<?> constraintDescriptor) {
		this.propertyPath = propertyPath;
		this.constraintDescriptor = constraintDescriptor;
	}

	public void disableDefaultError() {
		defaultDisabled = true;
	}

	public String getDefaultErrorMessageTemplate() {
		return ( String ) constraintDescriptor.getAttributes().get( "message" );
	}

	public ErrorBuilder buildErrorWithMessageTemplate(String messageTemplate) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
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
					new ErrorMessage( getDefaultErrorMessageTemplate(), propertyPath )
			);
		}
		return returnedErrorMessages;
	}

	public class ErrorMessage {
		private final String message;
		private final Path propertyPath;

		public ErrorMessage(String message, Path property) {
			this.message = message;
			this.propertyPath = property;
		}

		public String getMessage() {
			return message;
		}

		public Path getPath() {
			return propertyPath;
		}
	}
}
