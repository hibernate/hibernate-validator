package org.hibernate.validator.referenceguide.chapter06.payload;

import javax.validation.Payload;

public class Severity {
	public interface Info extends Payload {
	}

	public interface Error extends Payload {
	}
}
