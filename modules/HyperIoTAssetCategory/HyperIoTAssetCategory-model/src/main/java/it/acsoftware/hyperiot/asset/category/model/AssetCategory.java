package it.acsoftware.hyperiot.asset.category.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAssetOwnerImpl;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Aristide Cittadino Model class for AssetCategory of HyperIoT
 *         platform. This class maps the concept of Category that can be
 *         associated with any entity inside HyperIoT
 *
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "parent_id" }) })
public class AssetCategory extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {

	/**
	 * Name
	 */
	private String name;

	/**
	 * Who owns the category
	 */
	private HyperIoTAssetOwnerImpl owner;

	/**
	 * parent category
	 */
	private AssetCategory parent;

	private Set<AssetCategory> innerAssets;

	/**
	 * Resources associated with the current category
	 */
	private Set<AssetCategoryResource> resources;

	public AssetCategory() {
		super();
		this.resources = new HashSet<>();
	}

	/**
	 * 
	 * @return category name
	 */
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Size( max = 255)
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	@NotNullOnPersist
	public HyperIoTAssetOwnerImpl getOwner() {
		return owner;
	}

	/**
	 * 
	 * @param owner
	 */
	public void setOwner(HyperIoTAssetOwnerImpl owner) {
		this.owner = owner;
	}

	/**
	 * 
	 * @return Parent Category
	 */
	@ManyToOne(targetEntity = AssetCategory.class)
	public AssetCategory getParent() {
		return parent;
	}

	/**
	 * 
	 * @param parent
	 */
	public void setParent(AssetCategory parent) {
		this.parent = parent;
	}

	/**
	 *
	 * @return innerAssets
	 */
	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JsonBackReference
	public Set<AssetCategory> getInnerAssets() {
		return innerAssets;
	}

	/**
	 *
	 * @param innerAssets
	 */
	public void setInnerAssets(Set<AssetCategory> innerAssets) {
		this.innerAssets = innerAssets;
	}

	/**
	 * 
	 * @return resources associated with the current category
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "category", orphanRemoval = true, cascade = CascadeType.ALL)
	public Set<AssetCategoryResource> getResources() {
		return resources;
	}

	/**
	 * 
	 * @param resources resources to be set on current category
	 */
	public void setResources(Set<AssetCategoryResource> resources) {
		this.resources = resources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		AssetCategory other = (AssetCategory) obj;

		if (other.getId() > 0 && this.getId() > 0)
			return other.getId() == this.getId();

		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}