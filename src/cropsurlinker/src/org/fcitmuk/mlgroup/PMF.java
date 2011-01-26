/***
 * @author mistaguy
 * According to the App Engine docs, the PersistenceManagerFactory should only be created once in the application.
 */

package org.fcitmuk.mlgroup;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class PMF {
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {}

    public static PersistenceManagerFactory get() {
        return pmfInstance;
    }
}