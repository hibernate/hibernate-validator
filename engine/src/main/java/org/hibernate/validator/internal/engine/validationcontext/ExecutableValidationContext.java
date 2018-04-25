/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.reflect.Executable;
import java.util.Optional;

import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;

/**
 * Extension of {@link BaseBeanValidationContext} for executable validation.
 *
 * @author Marko Bekhta
 */
public interface ExecutableValidationContext<T> extends BaseBeanValidationContext<T> {

	Executable getExecutable();

	Optional<ExecutableMetaData> getExecutableMetaData();

}
