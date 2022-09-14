package it.acsoftware.hyperiot.base.model.authentication.principal;

import java.io.Serializable;
import java.security.Principal;

public class HyperIoTTopicPrincipal implements Principal, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String topic;
	private boolean canRead;
	private boolean canPublish;

	public HyperIoTTopicPrincipal(String topic) {
		super();
		this.topic = topic;
		this.canRead = true;
		this.canPublish = true;
	}

	public HyperIoTTopicPrincipal(String topic,boolean canRead,boolean canPublish) {
		super();
		this.topic = topic;
		this.canRead = canRead;
		this.canPublish = canPublish;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public void setCanPublish(boolean canPublish) {
		this.canPublish = canPublish;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public boolean isCanPublish() {
		return canPublish;
	}

	@Override
	public String getName() {
		return topic;
	}

}
