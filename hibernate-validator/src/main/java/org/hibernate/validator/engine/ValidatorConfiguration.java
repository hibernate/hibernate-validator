package org.hibernate.validator.engine;

import javax.validation.Configuration;

/**
 * Uniquely identify Hibernate Validator in the Bean Validation bootstrap strategy
 * Also contains Hibernate Validator specific configurations
 * 
 * @author Emmanuel Bernard
 */
public interface ValidatorConfiguration extends Configuration<ValidatorConfiguration> {
}
