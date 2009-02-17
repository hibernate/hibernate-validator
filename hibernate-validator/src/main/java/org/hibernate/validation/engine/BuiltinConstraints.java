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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.hibernate.validation.constraints.AssertFalseValidator;
import org.hibernate.validation.constraints.AssertTrueValidator;
import org.hibernate.validation.constraints.DigitsValidatorForNumber;
import org.hibernate.validation.constraints.DigitsValidatorForString;
import org.hibernate.validation.constraints.FutureValidatorForCalendar;
import org.hibernate.validation.constraints.FutureValidatorForDate;
import org.hibernate.validation.constraints.MaxValidatorForNumber;
import org.hibernate.validation.constraints.MaxValidatorForString;
import org.hibernate.validation.constraints.MinValidatorForNumber;
import org.hibernate.validation.constraints.MinValidatorForString;
import org.hibernate.validation.constraints.NotNullValidator;
import org.hibernate.validation.constraints.NullValidator;
import org.hibernate.validation.constraints.PastValidatorForCalendar;
import org.hibernate.validation.constraints.PastValidatorForDate;
import org.hibernate.validation.constraints.SizeValidatorForString;
import org.hibernate.validation.constraints.SizeValidatorForCollection;
import org.hibernate.validation.constraints.SizeValidatorForArray;
import org.hibernate.validation.constraints.SizeValidatorForMap;

/**
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 */
public class BuiltinConstraints {

	private final Map<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>> builtinConstraints =
			new HashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>>();

	public BuiltinConstraints() {

		List<Class<? extends ConstraintValidator<?, ?>>> constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertFalseValidator.class );
		builtinConstraints.put( AssertFalse.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertTrueValidator.class );
		builtinConstraints.put( AssertTrue.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( FutureValidatorForCalendar.class );
		constraintList.add( FutureValidatorForDate.class );
		builtinConstraints.put( Future.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MaxValidatorForNumber.class );
		constraintList.add( MaxValidatorForString.class );
		builtinConstraints.put( Max.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MinValidatorForNumber.class );
		constraintList.add( MinValidatorForString.class );
		builtinConstraints.put( Min.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NotNullValidator.class );
		builtinConstraints.put( NotNull.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NullValidator.class );
		builtinConstraints.put( Null.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( PastValidatorForCalendar.class );
		constraintList.add( PastValidatorForDate.class );
		builtinConstraints.put( Future.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( DigitsValidatorForString.class );
		constraintList.add( DigitsValidatorForNumber.class );
		builtinConstraints.put( Digits.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( SizeValidatorForString.class );
		constraintList.add( SizeValidatorForCollection.class );
		constraintList.add( SizeValidatorForArray.class );
		constraintList.add( SizeValidatorForMap.class );
		builtinConstraints.put( Size.class, constraintList );


	}

	public List<Class<? extends ConstraintValidator<?, ?>>> getBuiltInConstraints(Annotation annotation) {
		List<Class<? extends ConstraintValidator<?, ?>>> constraints = builtinConstraints.get( annotation.annotationType() );

		if ( constraints == null ) {
			throw new ValidationException( "Unable to find constraints for  " + annotation.annotationType() );
		}

		return constraints;
	}

}
