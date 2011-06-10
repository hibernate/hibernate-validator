package org.hibernate.validator.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.WebSafe;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Validate that the string does not contain malicious code.
 * 
 * @author George Gastaldi
 */
public class WebSafeValidator implements ConstraintValidator<WebSafe, String> {

    public void initialize(WebSafe constraintAnnotation) {
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
    	if (value == null) {
    		return true;
    	}
        return Jsoup.isValid(value, Whitelist.basic());
    }

}
