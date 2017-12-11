/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the annotated element as incubating. The contract of incubating elements (e.g. packages, types, methods,
 * constants etc.) is under active development and may be incompatibly altered - or removed - in subsequent releases.
 * <p>
 * Usage of incubating API/SPI members is encouraged (so the development team can get feedback on these new features)
 * but you should be prepared for updating code which is using them as needed.
 *
 * @author Gunnar Morling
 */
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface Incubating {
}
