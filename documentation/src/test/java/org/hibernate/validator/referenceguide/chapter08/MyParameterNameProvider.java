package org.hibernate.validator.referenceguide.chapter08;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import javax.validation.ParameterNameProvider;

public class MyParameterNameProvider implements ParameterNameProvider {

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		//...
		return null;
	}

	@Override
	public List<String> getParameterNames(Method method) {
		//...
		return null;
	}
}
