package it.acsoftware.hyperiot.company.repository;



import org.apache.aries.jpa.template.JpaTemplate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;

import it.acsoftware.hyperiot.company.api.CompanyRepository ;
import it.acsoftware.hyperiot.company.model.Company;

/**
 *
 * @author Aristide Cittadino Implementation class of the Company. This
 *         class is used to interact with the persistence layer.
 *
 */
@Component(service=CompanyRepository.class,immediate=true)
public class CompanyRepositoryImpl extends HyperIoTBaseRepositoryImpl<Company> implements CompanyRepository {
	/**
	 * Injecting the JpaTemplate to interact with database
	 */
	private JpaTemplate jpa;

	/**
	 * Constructor for a CompanyRepositoryImpl
	 */
	public CompanyRepositoryImpl() {
		super(Company.class);
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
	@Reference(target = "(osgi.unit.name=hyperiot-company-persistence-unit)")
	protected void setJpa(JpaTemplate jpa) {
		getLog().debug( "invoking setJpa, setting: {}" , jpa);
		this.jpa = jpa;
	}
}
