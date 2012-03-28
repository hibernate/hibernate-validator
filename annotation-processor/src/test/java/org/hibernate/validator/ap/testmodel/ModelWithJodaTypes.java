/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.constraints.Future;
import javax.validation.constraints.Past;

import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

public class ModelWithJodaTypes {

	@Past
	@Future
	public Date jdkDate;
	
	@Past
	@Future
	public GregorianCalendar jdkCalendar;
	
	@Past
	@Future
	public ReadableInstant jodaInstant;

	@Past
	@Future
	public DateMidnight jodaDateMidnight;
	
	@Past
	@Future
	public ReadablePartial jodaPartial;

	@Past
	@Future
	public LocalDate jodaLocalDate;
	
	/**
	 * Not allowed.
	 */
	@Future
	@Past
	public String string;
	
}
