package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Marko Bekhta
 */
public interface ImprovedCustomContainer<T, E> extends CustomContainer<T> {
	int size();

	E getE();
}
