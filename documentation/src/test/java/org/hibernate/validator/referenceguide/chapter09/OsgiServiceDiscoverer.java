//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.util.List;
import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

//tag::include[]
public class OsgiServiceDiscoverer implements ValidationProviderResolver {

	@Override
	public List<ValidationProvider<?>> getValidationProviders() {
		//...
		return null;
	}
}
//end::include[]
