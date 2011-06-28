/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.constraints.impl;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ModCheck.ModType;

/**
 * Mod check validator for MOD10 and MOD11 algorithms
 * 
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 * 
 * @author George Gastaldi
 * 
 */
public class ModCheckValidator implements ConstraintValidator<ModCheck, String> {

    private static final String NUMBERS_ONLY_REGEXP = "[^0-9]";
    private static final int DEC_RADIX = 10;
    private int multiplier;
    private int rangeStart;
    private int rangeEnd;
    private int checkDigitIndex;
    private ModType modType;

    public void initialize(ModCheck constraintAnnotation) {
        this.modType = constraintAnnotation.value();
        this.multiplier = constraintAnnotation.multiplier();
        this.rangeStart = constraintAnnotation.rangeStart();
        this.rangeEnd = constraintAnnotation.rangeEnd();
        this.checkDigitIndex = constraintAnnotation.checkDigitPosition();
        if ( this.rangeStart > this.rangeEnd ) {
            throw new IllegalArgumentException(
                    String.format( "Invalid Range: %d > %d", this.rangeStart, this.rangeEnd ) );
        }
    }

    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if ( value == null ) {
            return true;
        }
        boolean ret;
        String input = value.replaceAll( NUMBERS_ONLY_REGEXP, "" );
        // No need to test rangeEnd, because substring will consider the length of the string if over-limit
        if ( isRangeInbounds( checkDigitIndex, input ) && isRangeInbounds( rangeStart, input ) ) {
            String substring = input.substring( this.rangeStart, this.rangeEnd );
            if ( modType == ModType.MOD10 ) {
                ret = passesMod10Test( substring, multiplier );
            }
            else {
                ret = passesMod11Test( input, substring );
            }
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * Check if the input passes the mod11 test
     * 
     * @param input
     * @param substring
     * @return
     */
    public boolean passesMod11Test(String input, String substring) {
        boolean ret;
        int modResult = mod11( substring, this.multiplier );
        int digit = Character.digit( input.charAt( checkDigitIndex ), DEC_RADIX );
        ret = ( modResult == digit );
        return ret;
    }

    /**
     * Test if the range is inbounds
     * 
     * @param range
     * @param input
     * @return
     */
    private boolean isRangeInbounds(int range, String input) {
        return range >= 0 && range < input.length();
    }

    /**
     * Mod10 (Luhn) algorithm implementation
     * 
     * @param value
     * @param multiplicator
     * @return
     */
    public final boolean passesMod10Test(final String value, final int multiplicator) {
        List<Integer> digits = extractNumbers( value );
        int sum = 0;
        boolean even = false;
        for ( int index = digits.size() - 1; index >= 0; index-- ) {
            int digit = digits.get( index );
            if ( even ) {
                digit *= multiplicator;
            }
            if ( digit > 9 ) {
                digit = digit / 10 + digit % 10;
            }
            sum += digit;
            even = !even;
        }
        return sum % 10 == 0;
    }

    /**
     * Calculate Mod11
     * 
     * @param value
     *            extracts
     * @param max_weight
     *            maximum weight for multiplication
     * @return the result of the mod11 function
     */
    public final int mod11(final String value, final int max_weight) {
        int sum = 0;
        int weight = 2;

        List<Integer> digits = extractNumbers( value );
        for ( int index = digits.size() - 1; index >= 0; index-- ) {
            sum += digits.get( index ) * weight++;
            if ( weight > max_weight ) {
                weight = 2;
            }
        }
        int mod = 11 - ( sum % 11 );
        return ( mod > 9 ) ? 0 : mod;
    }

    /**
     * Parses the {@link String} value as a {@link List} of {@link Integer} objects
     * 
     * @param value
     *            the input string to be parsed
     * @return List of Integer objects. Ignores non-numeric chars
     */
    private List<Integer> extractNumbers(final String value) {
        List<Integer> digits = new ArrayList<Integer>( value.length() );
        char[] chars = value.toCharArray();
        for ( char c : chars ) {
            if ( Character.isDigit( c ) ) {
                digits.add( Character.digit( c, 10 ) );
            }
        }
        return digits;
    }

}
