package org.hibernate.validator.integrationtest.java.module.cdi.model;

import org.hibernate.validator.integrationtest.java.module.cdi.constraint.CarConstraint;
import org.hibernate.validator.integrationtest.java.module.cdi.constraint.CarServiceConstraint;

@CarConstraint
@CarServiceConstraint
public class Car {
}
