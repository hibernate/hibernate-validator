/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.sequence;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.GroupSequence;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.BasicConstraints;
import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.BasicConstraints.OverlyBasicConstraints;
import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.BasicConstraints.SomewhatBasicConstraints;
import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.ComplexConstraints;
import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.ComplexConstraints.ImmenselyComplexConstraints;
import org.hibernate.validator.test.internal.engine.groups.sequence.SequenceOfSequencesTest.AllConstraints.ComplexConstraints.SomewhatComplexConstraints;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

/**
 * Sequences may comprise other sequences.
 *
 * @author Gunnar Moring
 */
public class SequenceOfSequencesTest {

	/**
	 * A sequence of sequences is passed to the validate() call.
	 */
	@Test
	public void groupSequenceOfGroupSequences() {
		Validator validator = ValidatorUtil.getValidator();

		PlushAlligator alligator = new PlushAlligator();

		Set<ConstraintViolation<PlushAlligator>> violations = validator.validate( alligator, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "name" );

		alligator.name = "Ruben";
		violations = validator.validate( alligator, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "highestEducationalDegree" );

		alligator.highestEducationalDegree = "PhD";
		violations = validator.validate( alligator, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "length" );

		alligator.length = 540;
		violations = validator.validate( alligator, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "age" );
	}

	/**
	 * A sequence of sequences is used as the default group sequence.
	 */
	@Test
	@TestForIssue(jiraKey = "HV-1055")
	public void defaultGroupSequenceContainsOtherGroupSequences() {
		Validator validator = ValidatorUtil.getValidator();

		PlushCrocodile crocodile = new PlushCrocodile();

		Set<ConstraintViolation<PlushCrocodile>> violations = validator.validate( crocodile );
		assertCorrectPropertyPaths( violations, "name" );

		crocodile.name = "Ruben";
		violations = validator.validate( crocodile, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "highestEducationalDegree" );

		crocodile.highestEducationalDegree = "PhD";
		violations = validator.validate( crocodile, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "length" );

		crocodile.length = 540;
		violations = validator.validate( crocodile, AllConstraints.class );
		assertCorrectPropertyPaths( violations, "age" );
	}

	public static class PlushAlligator {

		@NotNull(groups = OverlyBasicConstraints.class)
		public String name;

		@NotNull(groups = SomewhatBasicConstraints.class)
		public String highestEducationalDegree;

		@NotNull(groups = SomewhatComplexConstraints.class)
		public Integer length;

		@NotNull(groups = ImmenselyComplexConstraints.class)
		public Integer age;
	}

	@GroupSequence({ AllConstraints.class, PlushCrocodile.class })
	public static class PlushCrocodile {

		@NotNull(groups = OverlyBasicConstraints.class)
		public String name;

		@NotNull(groups = SomewhatBasicConstraints.class)
		public String highestEducationalDegree;

		@NotNull(groups = SomewhatComplexConstraints.class)
		public Integer length;

		@NotNull(groups = ImmenselyComplexConstraints.class)
		public Integer age;
	}

	@GroupSequence({ BasicConstraints.class, ComplexConstraints.class })
	public interface AllConstraints {

		@GroupSequence({ OverlyBasicConstraints.class, SomewhatBasicConstraints.class })
		public interface BasicConstraints {

			public interface OverlyBasicConstraints {
			}

			public interface SomewhatBasicConstraints {
			}
		}

		@GroupSequence({ SomewhatComplexConstraints.class, ImmenselyComplexConstraints.class })
		public interface ComplexConstraints {

			public interface SomewhatComplexConstraints {
			}

			public interface ImmenselyComplexConstraints {
			}
		}
	}
}
