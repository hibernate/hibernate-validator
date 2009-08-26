package org.hibernate.validator.util;

import java.lang.reflect.Member;
import java.security.PrivilegedAction;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Emmanuel Bernard
 */
public class SetAccessibility implements PrivilegedAction<Object> {
	private final Member member;

	public static SetAccessibility action(Member member) {
		return new SetAccessibility( member );
	}

	private SetAccessibility(Member member) {
		this.member = member;
	}

	public Object run() {
		ReflectionHelper.setAccessibility( member );
		return member;
	}
}