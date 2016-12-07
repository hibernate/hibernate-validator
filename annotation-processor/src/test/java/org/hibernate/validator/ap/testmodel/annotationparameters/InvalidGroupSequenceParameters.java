/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.GroupSequence;

/**
 * @author Marko Bekhta
 */
public class InvalidGroupSequenceParameters {

	/**
	 * Case 1: class present in a list of groups
	 */
	public static class Case1 {
		public interface Group1 {
		}

		public class InvalidGroup1 {
		}

		public class InvalidGroup2 {
		}

		@GroupSequence(value = { Group1.class, InvalidGroup1.class })
		public class SomeBean {
		}

		@GroupSequence(value = InvalidGroup2.class)
		public class SomeOtherBean {
		}
	}

	/**
	 * Case 2: primitive present in a list of groups
	 */
	public static class Case2 {
		public interface Group1 {
		}

		public interface Group2 {
		}

		@GroupSequence(value = { Group1.class, int.class })
		public class SomeBean {
		}

		@GroupSequence(value = boolean.class)
		public class SomeOtherBean {
		}
	}

	/**
	 * Case 3: Cyclic groups
	 */
	public static class Case3 {
		@GroupSequence(value = Group1.class)
		public interface Group1 {
		}

		public interface Group2 {
		}

		public interface Group3 extends Group2 {
		}

		public interface Group4 extends Group3 {
		}

		@GroupSequence(value = Group3.class)
		public interface Group5 extends Group4, Group3 {
		}

		@GroupSequence(value = Group5.class)
		public interface Group6 {
		}
	}

	/**
	 * Case 4: Example of redefining a group sequence without using a class in a list
	 */
	public static class Case4 {
		public interface RentalChecks {
		}

		public interface CarChecks {
		}

		public class Car {
		}

		@GroupSequence({ RentalChecks.class, CarChecks.class })
		public class RentalCar extends Car {
		}
	}

	/**
	 * Case 5: Group sequence cyclic definition - incorrect
	 */
	public static class Case5 {
		@GroupSequence(Group2.class)
		public interface Group1 {
		}

		@GroupSequence(Group1.class)
		public interface Group2 {
		}
	}

	/**
	 * Case 6: Multiple usage of same interface in the same sequence
	 */
	public static class Case6 {
		public interface Group1 {
		}

		public interface Group2 {
		}

		@GroupSequence(value = { Group1.class, Group2.class, Group1.class })
		public interface MySequence {
		}
	}

	/**
	 * Case 7: Deeper cyclic definition - incorrect
	 */
	public static class Case7 {
		public interface Group1 {
		}

		public interface Group2 {
		}

		@GroupSequence(value = { Group1.class, Group4.class })
		public interface Group3 {
		}

		@GroupSequence(value = Group5.class)
		public interface Group4 {
		}

		@GroupSequence(value = { Group2.class, Group3.class })
		public interface Group5 {
		}
	}
}
