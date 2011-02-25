/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.method;

import java.lang.reflect.Method;
import java.util.Set;

import org.hibernate.validator.method.metadata.TypeDescriptor;

/**
 * <p>
 * Provides an API for method-level constraint validation, based on <a
 * href="http://jcp.org/en/jsr/detail?id=303">JSR 303: Bean Validation</a>,
 * Appendix C ("Proposal for method-level validation").
 * </p>
 * <p>
 * The purpose of this API is to provide a facility for the <a
 * href="http://en.wikipedia.org/wiki/Programming_by_contract">Programming by
 * Contract</a> approach to program design based on the concepts defined by the
 * Bean Validation API. More specifically this means that any Bean Validation
 * constraints (built-in as well as custom constraints) can be used to describe
 * </p>
 * <ul>
 * <li>
 * any preconditions that must be met before a method may legally be invoked (by annotating
 * method parameters with constraints) and</li>
 * <li>
 * any postconditions that are guaranteed after a method invocation returns (by
 * annotating methods).</li>
 * </ul>
 * <p>
 * These constraints may be declared directly on a method's parameters and/or return values.
 * Alternatively, by annotating method parameters/return values with the special {@link javax.validation.Valid}
 * annotation a recursive validation of those parameters/return
 * values against the constraints defined on their types can be triggered.
 * Consider for instance the method declaration
 * </p>
 * <p/>
 *
 * <pre>
 * &#064;NotNull
 * &#064;Valid
 * Customer findCustomerByName(&#064;NotNull &#064;Size(min = 5) String name) {
 * 	// ...
 * }
 * </pre>
 * <p>
 * Here, the validation engine will check for the following pre- and postconditions (provided
 * the method validation is triggered automatically by some integration layer, see below):
 * </p>
 * <ul>
 * <li>The name parameter is guaranteed to be not null and at least 5 characters
 * long. In particular it is not necessary for the implementor of this method to
 * perform these checks manually.</li>
 * <li>It is guaranteed that the call yields a non-null object, which itself is
 * valid according to all the bean validation constraints declared on its type.
 * In particular it is not necessary for the caller to perform these
 * checks manually.</li>
 * </ul>
 * <p>
 * This service only deals with the actual <em>validation</em> of method
 * parameters/return values itself, but not with the <em>invocation</em> of such
 * a validation. It is expected that this invocation is triggered by an
 * integration layer using AOP or similar method interception facilities such as
 * the JDK's {@link java.lang.reflect.Proxy} API or <a
 * href="http://jcp.org/en/jsr/detail?id=299">CDI</a> (
 * "JSR 299: Contexts and Dependency Injection for the Java<sup>TM</sup> EE platform"
 * ).
 * </p>
 * <p>
 * Such an integration layer would typically intercept each method call to be
 * validated, validate the call's parameters, proceed with the method invocation
 * and finally validate the invocation's return value. If any of the validation
 * steps yields a non-empty set of constraint violations the integration layer
 * would typically throw a {@link MethodConstraintViolationException} wrapping
 * these violations which in turn guarantees that the call flow only arrives at
 * the method's implementation respectively call site if all pre- respectively
 * postconditions are fulfilled.
 * </p>
 * <p>
 * <code>MethodValidator</code> instances are obtained by
 * {@link javax.validation.Validator#unwrap(Class) unwrapping} a {@link javax.validation.Validator} object:
 * </p>
 *
 * <pre>
 * Validator validator = ...;
 * MethodValidator methodValidator = validator.unwrap(MethodValidator.class);
 * </pre>
 * <p/>
 * Method level validation is (currently) a proprietary feature of Hibernate
 * Validator (HV), so the unwrapped <code>Validator</code> instance
 * <em>must</em> be HV's implementation. In case there are multiple Bean
 * Validation implementations on the classpath, this can be done be explicitly
 * choosing HV as validation provider:
 * <p/>
 *
 * <pre>
 * MethodValidator methodValidator = Validation.byProvider(HibernateValidator.class)
 * 	.configure()
 * 	.buildValidatorFactory()
 * 	.getValidator()
 * 	.unwrap(MethodValidator.class);
 * </pre>
 * <p/>
 * <p>
 * If not stated otherwise, none of this interface's methods allow
 * <code>null</code> as parameter value.
 * </p>
 *
 * @author Gunnar Morling
 */
public interface MethodValidator {

	/**
	 * Validates a given parameter of a given method.
	 *
	 * @param <T> The type hosting the invoked method.
	 * @param object The object on which the given method was invoked.
	 * @param method The invoked method for which the given parameter shall be
	 * validated.
	 * @param parameterValue The value provided by the caller for the given method.
	 * @param parameterIndex The index of the parameter to be validated within the given
	 * method's parameter array.
	 * @param groups A - potentially empty - number of validation groups for which
	 * the validation shall be performed. The @link {@link javax.validation.groups.Default}
	 * group will be validated if no group is given.
	 *
	 * @return A set with the constraint violations caused by this validation.
	 *         Will be empty, of no error occurs, but never null.
	 */
	<T> Set<MethodConstraintViolation<T>> validateParameter(T object,
															Method method,
															Object parameterValue,
															int parameterIndex,
															Class<?>... groups);

	/**
	 * Validates all parameters of a given method.
	 *
	 * @param <T> The type hosting the invoked method.
	 * @param object The object on which the given method was invoked.
	 * @param method The invoked method for which the given parameter shall be
	 * validated.
	 * @param parameterValues The values provided by the caller for the given method's
	 * parameters.
	 * @param groups A - potentially empty - number of validation groups for which
	 * the validation shall be performed. The @link {@link javax.validation.groups.Default}
	 * group will be validated if no group is given.
	 *
	 * @return A set with the constraint violations caused by this validation.
	 *         Will be empty, of no error occurs, but never null.
	 */
	<T> Set<MethodConstraintViolation<T>> validateAllParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);

	/**
	 * Validates the return value of a given method.
	 *
	 * @param <T> The type hosting the invoked method.
	 * @param object The object on which the given method was invoked.
	 * @param method The invoked method for which the given return value shall be
	 * validated.
	 * @param returnValue The value returned by the invoked method.
	 * @param groups A - potentially empty - number of validation groups for which
	 * the validation shall be performed. The @link {@link javax.validation.groups.Default}
	 * group will be validated if no group is given.
	 *
	 * @return A set with the constraint violations caused by this validation.
	 *         Will be empty, of no error occurs, but never null.
	 */
	<T> Set<MethodConstraintViolation<T>> validateReturnValue(T object,
															  Method method, Object returnValue, Class<?>... groups);

	/**
	 * Returns a descriptor providing access to constraint-related meta data for
	 * the given type.
	 *
	 * @param clazz The type of interest.
	 *
	 * @return A descriptor for the given type, will never be null.
	 */
	TypeDescriptor getConstraintsForType(Class<?> clazz);
}
