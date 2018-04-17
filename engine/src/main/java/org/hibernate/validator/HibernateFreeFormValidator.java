/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import java.util.Set;

import javax.json.JsonObject;
import javax.validation.ConstraintViolation;

/**
 * An interface for validating objects like JSON or Map.
 *
 * @author Marko Bekhta
 */
@Incubating
public interface HibernateFreeFormValidator {

	Set<ConstraintViolation<JsonObject>> validateJson(JsonObject json, Class<?> typeToValidate, Class<?>... groups);
}
