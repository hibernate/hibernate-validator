//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.util.List;

import jakarta.validation.ValidationProviderResolver;
import jakarta.validation.spi.ValidationProvider;

//tag::include[]
public class CustomValidationProviderResolver implements ValidationProviderResolver {

	@Override
	public List<ValidationProvider<?>> getValidationProviders() {
		//...
		//end::include[]
		List<ValidationProvider<?>> providers = null;
		//tag::include[]
		return providers;
	}
}
//end::include[]
