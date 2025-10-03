/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.IpAddress;

/**
 * An {@link IpAddress} constraint definition.
 *
 * @author Ivan Malutin
 * @since 9.1
 */
public class IpAddressDef extends ConstraintDef<IpAddressDef, IpAddress> {

    public IpAddressDef() {
        super(IpAddress.class);
    }

    public IpAddressDef type(IpAddress.Type type) {
        addParameter("type", type);
        return this;
    }
}
