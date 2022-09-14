package it.acsoftware.hyperiot.asset.category.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;

/**
 * 
 * @author Aristide Cittadino This class maps the concept of a resource (entity)
 *         associated with a category. This entity tracks association between
 *         entities and categories in a generic way.
 */
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "resourceName", "resourceId", "category_id" }) })
public class AssetCategoryResource extends HyperIoTAbstractEntity
		implements HyperIoTProtectedEntity {

	/**
	 * Resource name, tipically the entity class name
	 */
	private String resourceName;
	/**
	 * Entity primary key
	 */
	private long resourceId;
	/**
	 * The associated category
	 */
	private AssetCategory category;

	/**
	 * @return the resource name
	 */
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@NotBlank
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * 
	 * @param resourceName: entity resource name
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * 
	 * @return the resource primary key
	 */
	public long getResourceId() {
		return resourceId;
	}

	/**
	 * 
	 * @param resourceId the resource primary key
	 */
	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * 
	 * @return ManyToOne relationship with the category
	 */
	@ManyToOne(targetEntity = AssetCategory.class)
	public AssetCategory getCategory() {
		return category;
	}

	/**
	 * 
	 * @param category The associated category
	 */
	public void setCategory(AssetCategory category) {
		this.category = category;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + (int) (resourceId ^ (resourceId >>> 32));
		result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssetCategoryResource other = (AssetCategoryResource) obj;
		if (other.getId() > 0 && this.getId() > 0)
			return other.getId() == this.getId();

		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (resourceId != other.resourceId)
			return false;
		if (resourceName == null) {
			if (other.resourceName != null)
				return false;
		} else if (!resourceName.equals(other.resourceName))
			return false;
		return true;
	}

}
