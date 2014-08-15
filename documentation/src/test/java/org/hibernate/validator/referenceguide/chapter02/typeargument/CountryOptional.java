package org.hibernate.validator.referenceguide.chapter02.typeargument;

import java.util.Optional;
import javax.validation.Valid;

public class CountryOptional {
	@Valid
	Optional<@CustomNotBlank String> city = Optional.of( "" );
}
