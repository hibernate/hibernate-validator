package org.hibernate.validator.referenceguide.chapter03.parameterscriptassert;

import java.util.List;

import org.hibernate.validator.constraints.ParameterScriptAssert;

public class Car {

	@ParameterScriptAssert(lang = "javascript", script = "arg1.size() <= arg0.size() * 2")
	public void load(List<Person> passengers, List<PieceOfLuggage> luggage) {
		//...
	}
}
