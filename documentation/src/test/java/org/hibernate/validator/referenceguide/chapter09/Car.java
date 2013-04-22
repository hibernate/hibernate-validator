package org.hibernate.validator.referenceguide.chapter09;

import java.util.List;
import javax.validation.ConstraintTarget;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

@ValidCar
public class Car implements Vehicle {

	public interface SeverityInfo extends Payload {
	}

	private String manufacturer;

	@NotNull
	@Size(min = 2, max = 14)
	private String licensePlate;

	private Person driver;

	private String modelName;

	public Car() {
	}

	public Car(
			@NotNull String manufacturer,
			String licencePlate,
			Person driver,
			String modelName) {

		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
		this.driver = driver;
		this.modelName = modelName;
	}

	public void driveAway(@Max(75) int speed) {
		//...
	}

	@LuggageCountMatchesPassengerCount(
			piecesOfLuggagePerPassenger = 2,
			validationAppliesTo = ConstraintTarget.PARAMETERS,
			payload = SeverityInfo.class,
			message = "There must not be more than {piecesOfLuggagePerPassenger} pieces of " +
					"luggage per passenger."
	)
	public void load(List<Person> passengers, List<PieceOfLuggage> luggage) {
		//...
	}

	@Override
	@Size(min = 3)
	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@Valid
	@ConvertGroup(from = Default.class, to = Person.Basic.class)
	public Person getDriver() {
		return driver;
	}

	//further getters and setters...
}
