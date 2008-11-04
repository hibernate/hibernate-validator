package org.hibernate.validation;

import javax.validation.ValidatorFactoryBuilder;

/**
 * Uniquely identify Hibernate Validator in the Bean Validation bootstrap strategy
 * Also contains Hibernate Validator specific configurations
 * 
 * @author Emmanuel Bernard
 */
public interface HibernateValidatorFactoryBuilder extends ValidatorFactoryBuilder<HibernateValidatorFactoryBuilder> {
}
