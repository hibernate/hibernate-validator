package org.hibernate.validator.referenceguide.chapter02.typeargument;

import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;

public class CountryList {
	@Valid
	List<@CustomNotBlank String> cities = Arrays.asList( "First", "", "Third" );
}
