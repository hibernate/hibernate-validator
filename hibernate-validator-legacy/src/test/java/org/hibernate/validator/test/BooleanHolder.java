package org.hibernate.validator.test;

import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.AssertTrue;

/**
 * @author Hardy Ferentschik
 */
public class BooleanHolder {
    @AssertTrue
    private Boolean mustBeTrue;

    @AssertFalse
    private Boolean mustBeFalse;

    public Boolean getMustBeTrue() {
        return mustBeTrue;
    }

    public void setMustBeTrue(Boolean mustBeTrue) {
        this.mustBeTrue = mustBeTrue;
    }

    public Boolean getMustBeFalse() {
        return mustBeFalse;
    }

    public void setMustBeFalse(Boolean mustBeFalse) {
        this.mustBeFalse = mustBeFalse;
    }
}
