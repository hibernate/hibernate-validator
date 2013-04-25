package org.hibernate.validator.referenceguide.chapter08;

import java.util.Locale;
import javax.validation.MessageInterpolator;

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
