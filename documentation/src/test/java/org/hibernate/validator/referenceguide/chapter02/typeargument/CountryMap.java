package org.hibernate.validator.referenceguide.chapter02.typeargument;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;

public class CountryMap {
	@Valid
	public Map<Integer, @CustomNotBlank String> cities = new HashMap<>();
}
