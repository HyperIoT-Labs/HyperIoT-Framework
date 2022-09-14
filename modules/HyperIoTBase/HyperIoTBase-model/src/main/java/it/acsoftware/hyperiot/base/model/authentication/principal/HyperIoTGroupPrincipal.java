package it.acsoftware.hyperiot.base.model.authentication.principal;

import java.io.Serializable;
import java.security.Principal;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;

public class HyperIoTGroupPrincipal extends GroupPrincipal implements Principal, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public HyperIoTGroupPrincipal(String groupName) {
		super(groupName);
	}

}
