package org.hibernate.validation;

import javax.validation.ValidatorBuilder;

/**
 * Uniquely identify Hibernate Validator in the Bean Validation bootstrap strategy
 * Also contains Hibernate Validator specific configurations
 * 
 * @author Emmanuel Bernard
 */
public interface HibernateValidatorBuilder extends ValidatorBuilder<HibernateValidatorBuilder> {
}
