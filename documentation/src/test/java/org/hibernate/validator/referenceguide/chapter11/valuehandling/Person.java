package org.hibernate.validator.referenceguide.chapter11.valuehandling;

import java.util.Optional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class Person {

	//tag::optional[]
	@Size(min = 3)
	private Optional<String> firstName = Optional.of( "John" );

	@NotNull
	private Optional<String> lastName = Optional.of( "Doe" );
	//end::optional[]

	//tag::javafx[]
	@Min(value = 3)
	IntegerProperty integerProperty1 = new SimpleIntegerProperty( 4 );

	@Min(value = 3)
	Property<Number> integerProperty2 = new SimpleIntegerProperty( 4 );

	@Min(value = 3)
	ObservableValue<Number> integerProperty3 = new SimpleIntegerProperty( 4 );
	//end::javafx[]

	//tag::javafxUnwrapValidatedValue[]
	@Size(min = 3)
	private Property<String> name = new SimpleStringProperty( "Bob" );
	//end::javafxUnwrapValidatedValue[]

}
