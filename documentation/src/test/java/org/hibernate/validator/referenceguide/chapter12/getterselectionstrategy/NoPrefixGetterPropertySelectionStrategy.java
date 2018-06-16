//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.getterselectionstrategy;

//end::include[]
import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

//tag::include[]
public class NoPrefixGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

	@Override
	public boolean isGetter(ConstrainableExecutable executable) {
		// We check that the method has non void return type and no parameters.
		// And we do not care about method name at all.
		return executable.getReturnType() != void.class
				&& executable.getParameterTypes().length == 0;
	}

	@Override
	public String getPropertyName(ConstrainableExecutable method) {
		return method.getName();
	}

	@Override
	public Set<String> getGetterMethodNameCandidates(String propertyName) {
		// As method name == property name there always is just one possible name for a method
		return Collections.singleton( propertyName );
	}
}
//end::include[]
