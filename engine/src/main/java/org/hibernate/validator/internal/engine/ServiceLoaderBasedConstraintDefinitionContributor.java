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

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;

import com.fasterxml.classmate.ResolvedType;

import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * @author Hardy Ferentschik
 */
public class ServiceLoaderBasedConstraintDefinitionContributor implements ConstraintDefinitionContributor {
	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	public ServiceLoaderBasedConstraintDefinitionContributor(TypeResolutionHelper typeResolutionHelper) {
		this.typeResolutionHelper = typeResolutionHelper;
	}

	@Override
	public void collectConstraintDefinitions(ConstraintDefinitionBuilder constraintDefinitionContributionBuilder) {
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

		for ( Map.Entry<Class<?>, List<Class<?>>> entry : customValidators.entrySet() ) {
			registerConstraintDefinition( constraintDefinitionContributionBuilder, entry.getKey(), entry.getValue() );
		}
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> void registerConstraintDefinition(ConstraintDefinitionBuilder builder,
			Class<?> constraintType,
			List<Class<?>> validatorTypes) {
		ConstraintDefinitionBuilderContext<A> context = builder
				.constraint( (Class<A>) constraintType )
				.includeExistingValidators( true );

		for ( Class<?> validatorType : validatorTypes ) {
			context.validatedBy( (Class<? extends ConstraintValidator<A, ?>>) validatorType );
		}
	}

	private Class<?> determineAnnotationType(Class<?> constraintValidatorClass) {
		ResolvedType resolvedType = typeResolutionHelper.getTypeResolver()
				.resolve( constraintValidatorClass );

		return resolvedType.typeParametersFor( ConstraintValidator.class ).get( 0 ).getErasedType();
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


