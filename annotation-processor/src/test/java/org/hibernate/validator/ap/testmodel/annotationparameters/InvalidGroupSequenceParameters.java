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
	 * Case 3: Group sequence extending another interface
	 */
	public static class Case3 {
		public interface Group1 {
		}

		public interface ParentInterface {
		}

		@GroupSequence(value = Group1.class)
		public interface GroupSequence1 extends ParentInterface {
		}
	}

	/**
	 * Case 4: Example of redefining a group sequence without declaring the class in the group sequence
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
		@GroupSequence(GroupSequence2.class)
		public interface GroupSequence1 {
		}

		@GroupSequence(GroupSequence1.class)
		public interface GroupSequence2 {
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

		@GroupSequence(value = { Group1.class, GroupSequence2.class })
		public interface GroupSequence1 {
		}

		@GroupSequence(value = GroupSequence3.class)
		public interface GroupSequence2 {
		}

		@GroupSequence(value = { Group2.class, GroupSequence1.class })
		public interface GroupSequence3 {
		}
	}

	/**
	 * Case 8: Cyclic definition due to group inheritance - incorrect
	 */
	public static class Case8 {
		public interface Group1 extends Group2 {
		}

		public interface Group2 {
		}

		@GroupSequence(value = { Group1.class, Group2.class })
		public interface GroupSequence1 {
		}
	}

	/**
	 * Case 9: Cyclic definition due to group inheritance and group sequence inheritance - incorrect
	 */
	public static class Case9 {
		public interface Group1 extends Group2 {
		}

		public interface Group2 {
		}

		@GroupSequence(value = Group1.class)
		public interface GroupSequence1 {
		}

		@GroupSequence(value = GroupSequence1.class)
		public interface GroupSequence2 {
		}

		@GroupSequence(value = { Group2.class, GroupSequence2.class })
		public interface GroupSequence3 {
		}

	}
}
