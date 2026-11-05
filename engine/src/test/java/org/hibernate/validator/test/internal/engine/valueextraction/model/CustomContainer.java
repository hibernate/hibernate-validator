package org.hibernate.validator.test.internal.engine.valueextraction.model;

import java.util.Iterator;

/**
 * @author Marko Bekhta
 */
public interface CustomContainer<T> {

	Iterator<T> iterator();
}
