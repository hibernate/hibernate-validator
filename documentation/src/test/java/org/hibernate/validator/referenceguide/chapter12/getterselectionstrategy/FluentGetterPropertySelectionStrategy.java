//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.getterselectionstrategy;

import java.lang.reflect.Method;
import java.util.Arrays;
//end::include[]
import java.util.Collections;
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
	public boolean isGetter(ConstrainableExecutable executable) {
		// We check that the method has a non-void return type and no parameters.
		// And we do not care about the method name.
		return !methodNamesToIgnore.contains( executable.getName() )
				&& executable.getReturnType() != void.class
				&& executable.getParameterTypes().length == 0;
	}

	@Override
	public String getPropertyName(ConstrainableExecutable method) {
		return method.getName();
	}

	@Override
	public Set<String> getGetterMethodNameCandidates(String propertyName) {
		// As method name == property name, there always is just one possible name for a method
		return Collections.singleton( propertyName );
	}
}
//end::include[]
