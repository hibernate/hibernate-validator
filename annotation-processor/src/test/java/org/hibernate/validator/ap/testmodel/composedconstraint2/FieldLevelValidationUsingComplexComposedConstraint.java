/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FieldLevelValidationUsingComplexComposedConstraint {

	@ComposedConstraint
	public String string;

	@ComposedConstraint
	public List<?> list;

	/**
	 * Allowed
	 */
	@ComposedConstraint
	public GregorianCalendar gregorianCalendar;

	@ComposedConstraint
	public Collection<?> collection;

	/**
	 * Allowed
	 */
	@ComposedConstraint
	public ArrayList<?> arrayList;

	@ComposedConstraint
	public Calendar calendar;

	/**
	 * Not allowed.
	 */
	@ComposedConstraint
	public Date date;

}
