/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

		public interface Group2 {
		}

		@GroupSequence(value = Group2.class)
		public interface Group3 {
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

		@GroupSequence(value = { Group3.class, YetOtherBean.class })
		public class YetOtherBean {

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

		@GroupSequence(value = double.class)
		public class YetOtherBean {

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

		@GroupSequence(value = Group2.class)
		public interface Group3 extends Group2 {
		}

		public interface Group4 extends Group3 {
		}

		@GroupSequence(value = Group2.class)
		public interface Group5 extends Group4, Group3 {
		}

		@GroupSequence(value = Group2.class)
		public interface Group6 extends Group5 {
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
			@NotNull
			private String manufacturer;

			@NotNull
			@Size(min = 2, max = 14)
			private String licensePlate;

			@Min(2)
			private int seatCount;

			@AssertTrue(
					message = "The car has to pass the vehicle inspection first",
					groups = CarChecks.class
			)
			private boolean passedVehicleInspection;

			public Car(String manufacturer, String licencePlate, int seatCount) {
				this.manufacturer = manufacturer;
				this.licensePlate = licencePlate;
				this.seatCount = seatCount;
			}

			// getters and setters ...
		}

		@GroupSequence({ RentalChecks.class, CarChecks.class })
		public class RentalCar extends Car {
			@AssertFalse(message = "The car is currently rented out", groups = RentalChecks.class)
			private boolean rented;

			public RentalCar(String manufacturer, String licencePlate, int seatCount) {
				super( manufacturer, licencePlate, seatCount );
			}

			public boolean isRented() {
				return rented;
			}

			public void setRented(boolean rented) {
				this.rented = rented;
			}
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
}
