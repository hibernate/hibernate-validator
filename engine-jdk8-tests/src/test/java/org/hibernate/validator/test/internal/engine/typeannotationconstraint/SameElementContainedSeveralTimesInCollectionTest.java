/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.typeannotationconstraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-1031")
public class SameElementContainedSeveralTimesInCollectionTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void sameInvalidInstanceInListShouldBeReportedWithAllPaths() {
		ListContainer listContainer = new ListContainer( Arrays.asList( "", "A", "" ) );

		Set<ConstraintViolation<ListContainer>> constraintViolations = validator.validate( listContainer );

		assertCorrectPropertyPaths( constraintViolations, "values[0].<collection element>", "values[2].<collection element>" );
	}

	@Test
	public void sameInvalidInstanceInMapShouldBeReportedWithAllPaths() {
		List<String> emptyList = Collections.emptyList();
		List<String> nonEmptyList = Arrays.asList( "A" );

		HashMap<String, List<String>> values = new HashMap<>();
		values.put( "NON_EMPTY", nonEmptyList );
		values.put( "EMPTY_1", emptyList );
		values.put( "EMPTY_2", emptyList );
		MapContainer withMap = new MapContainer( values );

		Set<ConstraintViolation<MapContainer>> constraintViolations = validator.validate( withMap );

		assertCorrectPropertyPaths( constraintViolations, "values[EMPTY_1].<collection element>", "values[EMPTY_2].<collection element>" );
	}

	private static class ListContainer {

		@Valid
		public List<@SizeWithTypeUse(min = 1) String> values;

		public ListContainer(List<String> values) {
			this.values = values;
		}
	}

	private static class MapContainer {

		@Valid
		public Map<String, @SizeWithTypeUse(min = 1) List<String>> values;

		public MapContainer(Map<String, List<String>> values) {
			this.values = values;
		}
	}

	// TODO Remove once the original one supports TYPE_USE
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = {})
	@Size
	public @interface SizeWithTypeUse {

		String message() default "{javax.validation.constraints.Size.message}";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};

		@OverridesAttribute(constraint = Size.class, name = "min")
		int min() default 0;

		@OverridesAttribute(constraint = Size.class, name = "max")
		int max() default Integer.MAX_VALUE;
	}
}
