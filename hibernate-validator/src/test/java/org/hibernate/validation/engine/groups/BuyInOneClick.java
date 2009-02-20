package org.hibernate.validation.engine.groups;

import javax.validation.groups.Default;

/**
 * Customer can buy without being harrassed by the checking-out process.
 *
 * @author Emmanuel Bernard
 */
public interface BuyInOneClick extends Default, Billable {
}