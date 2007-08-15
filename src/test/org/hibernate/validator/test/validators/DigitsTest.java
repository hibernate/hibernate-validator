//$Id: $
package org.hibernate.validator.test.validators;

import java.math.BigDecimal;

import org.hibernate.cfg.Configuration;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.test.HANTestCase;
import org.hibernate.validator.event.ValidateEventListener;

/**
 * @author Emmanuel Bernard
 */
public class DigitsTest extends HANTestCase {

	public void testDigits() throws Exception {
		Car car = new Car();
		car.name = "350Z";
		car.insurances = new String[] { "random" };
		car.length = new BigDecimal(10.2);
		car.gallons = 100.3;
		ClassValidator<Car> classValidator = new ClassValidator<Car>( Car.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 2, invalidValues.length );
		car.length = new BigDecimal(1.223); //more than 2
		car.gallons = 10.300; //1 digit really so not invalid
		invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 1, invalidValues.length );
	}

	public void testApply() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( Car.class.getName() );
		Column stateColumn = (Column) classMapping.getProperty( "gallons" ).getColumnIterator().next();
		assertEquals( stateColumn.getPrecision(), 3 );
		assertEquals( stateColumn.getScale(), 1 );
	}

	protected void configure(Configuration cfg) {
		cfg.getEventListeners()
				.setPreInsertEventListeners( new PreInsertEventListener[]{new ValidateEventListener()} );
		cfg.getEventListeners()
				.setPreUpdateEventListeners( new PreUpdateEventListener[]{new ValidateEventListener()} );
	}

	protected Class[] getMappings() {
		return new Class[] {
				Car.class
		};
	}
}
