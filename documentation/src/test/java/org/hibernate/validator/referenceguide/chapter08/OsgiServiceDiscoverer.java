package org.hibernate.validator.referenceguide.chapter08;

import java.util.List;
import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

public class OsgiServiceDiscoverer implements ValidationProviderResolver {

	@Override
	public List<ValidationProvider<?>> getValidationProviders() {
		//...
		return null;
	}
}
