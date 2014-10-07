/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;
import org.hibernate.validator.spi.constraintvalidator.ConstraintValidatorContribution;
import org.hibernate.validator.spi.constraintvalidator.ConstraintValidatorLocator;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * @author Hardy Ferentschik
 */
public class ServiceLoaderBasedConstraintValidatorLocator implements ConstraintValidatorLocator {
	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	public ServiceLoaderBasedConstraintValidatorLocator(TypeResolutionHelper typeResolutionHelper) {
		this.typeResolutionHelper = typeResolutionHelper;
	}

	@Override
	public List<ConstraintValidatorContribution<?>> getConstraintValidatorContributions() {
		Map<Class<?>, List<Class<?>>> customValidators = newHashMap();

		// find additional constraint validators via the Java ServiceLoader mechanism
		GetConstraintValidatorList constraintValidatorListAction = new GetConstraintValidatorList();
		List<ConstraintValidator<?, ?>> discoveredConstraintValidators = run( constraintValidatorListAction );

		for ( ConstraintValidator<?, ?> constraintValidator : discoveredConstraintValidators ) {
			Class<?> constraintValidatorClass = constraintValidator.getClass();
			Class<?> annotationType = determineAnnotationType( constraintValidatorClass );

			List<Class<?>> validators = customValidators.get( annotationType );
			if ( annotationType != null && validators == null ) {
				validators = new ArrayList<Class<?>>();
				customValidators.put( annotationType, validators );
			}
			validators.add( constraintValidatorClass );
		}

		List<ConstraintValidatorContribution<?>> constraintValidatorContributions = new ArrayList<ConstraintValidatorContribution<?>>();
		for ( Map.Entry<Class<?>, List<Class<?>>> entry : customValidators.entrySet() ) {
			@SuppressWarnings("unchecked")
			ConstraintValidatorContribution constraintValidatorContribution = new ConstraintValidatorContribution(
					entry.getKey(), entry.getValue(), true
			);
			constraintValidatorContributions.add( constraintValidatorContribution );
		}
		return constraintValidatorContributions;
	}

	private Class<?> determineAnnotationType(Class<?> constraintValidatorClass) {
		ResolvedType resolvedType = typeResolutionHelper.getTypeResolver()
				.resolve( constraintValidatorClass );

		MemberResolver memberResolver = new MemberResolver( typeResolutionHelper.getTypeResolver() );
		memberResolver.setMethodFilter( new InitializeMethodFilter( resolvedType ) );
		ResolvedTypeWithMembers typeWithMembers = memberResolver.resolve(
				resolvedType,
				null,
				null
		);

		ResolvedMethod[] resolvedMethods = typeWithMembers.getMemberMethods();
		return resolvedMethods[0].getArgumentType( 0 ).getErasedType();
	}

	/**
	 * A filter implementation filtering the constraint validator initialize method.
	 */
	private static class InitializeMethodFilter implements Filter<RawMethod> {
		private final ResolvedType resolvedType;
		private final Class<?> constraintType;

		private InitializeMethodFilter(ResolvedType resolvedType) {
			this.resolvedType = resolvedType;

			ResolvedType constraintValidator = null;
			for ( ResolvedType implementedInterface : resolvedType.getImplementedInterfaces() ) {
				if ( implementedInterface.getErasedType().equals( ConstraintValidator.class ) ) {
					constraintValidator = implementedInterface;
					break;
				}
			}

			if ( constraintValidator == null ) {
				// cannot happen
				Contracts.assertNotNull( "not a valid constraint validator" );
			}

			// the first type parameter of a constraint validator is the constraint type
			constraintType = constraintValidator.getTypeBindings().getTypeParameters().get( 0 ).getErasedType();
		}

		@Override
		public boolean include(RawMethod method) {
			if ( method.getDeclaringType() != resolvedType ) {
				return false;
			}

			if ( !"initialize".equals( method.getName() ) ) {
				return false;
			}

			Class<?>[] parameterTypes = method.getRawMember().getParameterTypes();
			if ( parameterTypes.length != 1 ) {
				return false;
			}

			if ( !parameterTypes[0].equals( constraintType ) ) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}


