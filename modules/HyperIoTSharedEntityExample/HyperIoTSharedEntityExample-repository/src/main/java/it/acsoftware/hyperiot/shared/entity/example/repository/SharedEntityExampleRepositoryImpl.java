package it.acsoftware.hyperiot.shared.entity.example.repository;



import org.apache.aries.jpa.template.JpaTemplate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;

import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleRepository ;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;

/**
 *
 * @author Aristide Cittadino Implementation class of the SharedEntityExample. This
 *         class is used to interact with the persistence layer.
 *
 */
@Component(service=SharedEntityExampleRepository.class,immediate=true)
public class SharedEntityExampleRepositoryImpl extends HyperIoTBaseRepositoryImpl<SharedEntityExample> implements SharedEntityExampleRepository {
	/**
	 * Injecting the JpaTemplate to interact with database
	 */
	private JpaTemplate jpa;

	/**
	 * Constructor for a SharedEntityExampleRepositoryImpl
	 */
	public SharedEntityExampleRepositoryImpl() {
		super(SharedEntityExample.class);
	}

	/**
	 *
	 * @return The current jpaTemplate
	 */
	@Override
	protected JpaTemplate getJpa() {
		getLog().debug( "invoking getJpa, returning: {}" , jpa);
		return jpa;
	}

	/**
	 * @param jpa Injection of JpaTemplate
	 */
	@Override
	@Reference(target = "(osgi.unit.name=hyperiot-sharedEntityExample-persistence-unit)")
	protected void setJpa(JpaTemplate jpa) {
		getLog().debug( "invoking setJpa, setting: " + jpa);
		this.jpa = jpa;
	}
}
