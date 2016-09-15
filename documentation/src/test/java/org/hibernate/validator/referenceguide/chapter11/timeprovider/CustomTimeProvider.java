//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.timeprovider;

//end::include[]

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.hibernate.validator.spi.time.TimeProvider;

//tag::include[]
public class CustomTimeProvider implements TimeProvider {

	@Override
	public long getCurrentTime() {
		Calendar now = GregorianCalendar.getInstance();
		return now.getTimeInMillis();
	}
}
//end::include[]
