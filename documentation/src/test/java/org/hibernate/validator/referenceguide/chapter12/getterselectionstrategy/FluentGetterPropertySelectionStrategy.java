//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.getterselectionstrategy;

//end::include[]
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

//tag::include[]
public class FluentGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

	private final Set<String> methodNamesToIgnore;

	public FluentGetterPropertySelectionStrategy() {
		// we will ignore all the method names coming from Object
		this.methodNamesToIgnore = Arrays.stream( Object.class.getDeclaredMethods() )
				.map( Method::getName )
				.collect( Collectors.toSet() );
	}

	@Override
	public Optional<String> getProperty(ConstrainableExecutable executable) {
		if ( methodNamesToIgnore.contains( executable.getName() )
				|| executable.getReturnType() == void.class
				|| executable.getParameterTypes().length > 0 ) {
			return Optional.empty();
		}

		return Optional.of( executable.getName() );
	}

	@Override
	public Set<String> getGetterMethodNameCandidates(String propertyName) {
		// As method name == property name, there always is just one possible name for a method
		return Collections.singleton( propertyName );
	}
}
//end::include[]
