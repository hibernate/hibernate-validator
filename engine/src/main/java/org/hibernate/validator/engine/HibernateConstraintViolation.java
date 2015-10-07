package org.hibernate.validator.engine;

import javax.validation.ConstraintViolation;

public interface HibernateConstraintViolation<T> extends ConstraintViolation<T> {
	Object getInfo();
}
