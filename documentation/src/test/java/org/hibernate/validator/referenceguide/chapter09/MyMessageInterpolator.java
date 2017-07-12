//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.util.Locale;
import javax.validation.MessageInterpolator;

//tag::include[]
public class MyMessageInterpolator implements MessageInterpolator {

	@Override
	public String interpolate(String messageTemplate, Context context) {
		//...
		return null;
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		//...
		return null;
	}
}
//end::include[]
