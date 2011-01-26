/***
 * @author mistaguy
 * This is a GAE Data persistent manager
 * An app interacts with JPA using an instance of the EntityManager class.
 * You get this instance by instantiating and calling a method on an instance of the EntityManagerFactory class. 
 * The factory uses the JPA configuration (identified by the name "transactions-optional") to create EntityManager instances.
 * Because an EntityManagerFactory instance takes time to initialize, 
 * it's a good idea to reuse a single instance as much as possible.
 *  An easy way to do this is to create a singleton wrapper class with a static instance, as follows:
  */

package org.fcitmuk.mlgroup;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class EMF {
	private static final EntityManagerFactory emfInstance = Persistence
			.createEntityManagerFactory("transactions-optional");

	private EMF() {
	}

	public static EntityManagerFactory get() {
		return emfInstance;
	}
}