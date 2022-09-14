package it.acsoftware.hyperiot.base.model.authentication.principal;

import java.io.Serializable;
import java.security.Principal;

import org.apache.karaf.jaas.boot.principal.UserPrincipal;

public class HyperIoTPrincipal extends UserPrincipal implements Principal, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private boolean isAdmin;

	public HyperIoTPrincipal(String name,boolean isAdmin) {
		super(name);
		this.isAdmin = isAdmin;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

}
