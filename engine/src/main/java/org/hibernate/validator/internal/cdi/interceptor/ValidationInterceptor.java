/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.cdi.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.spi.MethodValidated;

/**
 * An interceptor which performs a validation of the Bean Validation constraints specified at the parameters and/or return
 * values of intercepted methods using the method validation functionality provided by Hibernate Validator.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
@MethodValidated
@Interceptor
public class ValidationInterceptor implements Serializable {

	private static final long serialVersionUID = 604440259030722151L;

	/**
	 * The validator to be used for method validation.
	 * <p/>
	 * Although the concrete validator is not necessarily serializable (and HV's implementation indeed isn't) it is still
	 * alright to have it as non-transient field here. Upon passivation not the validator itself will be serialized, but the
	 * proxy injected here, which in turn is serializable.
	 */
	@Inject
	private Validator validator;

	/**
	 * Validates the Bean Validation constraints specified at the parameters and/or return value of the intercepted method.
	 *
	 * @param ctx The context of the intercepted method invocation.
	 *
	 * @return The result of the method invocation.
	 *
	 * @throws Exception Any exception caused by the intercepted method invocation. A {@link ConstraintViolationException}
	 * in case at least one constraint violation occurred either during parameter or return value validation.
	 */
	@AroundInvoke
	public Object validateMethodInvocation(InvocationContext ctx) throws Exception {
		Set<ConstraintViolation<Object>> violations = validator.forMethods().validateParameters(
				ctx.getTarget(),
				ctx.getMethod(),
				ctx.getParameters()
		);

		// TODO - need to figure out why I cannot cast here. Works in the IDE, but on the command line the build fails (HF)
		Set<ConstraintViolation<?>> violationsNew = new HashSet<ConstraintViolation<?>>();
		violationsNew.addAll( violations );

		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException(
					getMessage( ctx.getMethod(), ctx.getParameters(), violations ),
					violationsNew
			);
		}

		Object result = ctx.proceed();

		violations = validator.forMethods().validateReturnValue(
				ctx.getTarget(),
				ctx.getMethod(),
				result
		);

		violationsNew = new HashSet<ConstraintViolation<?>>();
		violationsNew.addAll( violations );

		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException(
					getMessage( ctx.getMethod(), ctx.getParameters(), violations ),
					violationsNew
			);
		}

		return result;
	}

	private String getMessage(Method method, Object[] args, Set<? extends ConstraintViolation<?>> violations) {

		StringBuilder message = new StringBuilder();
		message.append( violations.size() );
		message.append( " constraint violation(s) occurred during method invocation." );
		message.append( "\nMethod: " );
		message.append( method );
		message.append( "\nArgument values: " );
		message.append( Arrays.toString( args ) );
		message.append( "\nConstraint violations: " );

		int i = 1;
		for ( ConstraintViolation<?> constraintViolation : violations ) {
			ElementDescriptor elementDescriptor = locateElementDescriptor( constraintViolation );

			message.append( "\n (" );
			message.append( i );
			message.append( ") Kind: " );
			message.append( elementDescriptor.getKind() );
			if ( elementDescriptor instanceof ParameterDescriptor ) {
				message.append( "\n parameter index: " );
				message.append( ( (ParameterDescriptor) elementDescriptor ).getIndex() );
			}
			message.append( "\n message: " );
			message.append( constraintViolation.getMessage() );
			message.append( "\n root bean: " );
			message.append( constraintViolation.getRootBean() );
			message.append( "\n property path: " );
			message.append( constraintViolation.getPropertyPath() );
			message.append( "\n constraint: " );
			message.append( constraintViolation.getConstraintDescriptor().getAnnotation() );

			i++;
		}

		return message.toString();
	}

	private ElementDescriptor locateElementDescriptor(ConstraintViolation<?> constraintViolation) {
		Iterator<Path.Node> nodes = constraintViolation.getPropertyPath().iterator();
		Path.Node leafNode = null;
		while ( nodes.hasNext() ) {
			leafNode = nodes.next();
		}
		return leafNode.getElementDescriptor();
	}
}


