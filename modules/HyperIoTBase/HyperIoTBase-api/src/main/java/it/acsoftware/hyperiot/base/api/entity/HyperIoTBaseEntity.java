package it.acsoftware.hyperiot.base.api.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTResource;

import java.util.Date;

/**
 * @author Aristide Cittadino Generic Interface Component for
 * HyperIoTBaseEntity. This interface defines methods for obtaining and
 * setting the entity id of the HyperIoT platform.
 */
public interface HyperIoTBaseEntity extends HyperIoTResource {
    /**
     * Gets the entity id
     *
     * @return entity id
     */
    long getId();

    /**
     * Sets the entity id
     *
     * @param id set up the entity id
     */
    void setId(long id);

    /**
     * Not persistend on database used to reference categories with the
     * HyperIoTAssetCategoryManager
     *
     * @return categoryIds
     */
    long[] getCategoryIds();

    /**
     * @param categoryIds category Ids
     */
    void setCategoryIds(long[] categoryIds);

    /**
     *
     * @return the creatiion timestamp
     */
    Date getEntityCreateDate();

    /**
     *
     * @param createDate Create Entity timestamp
     */
    public void setEntityCreateDate(Date createDate);

    /**
     *
     * @return the entity version
     */
    int getEntityVersion();

    /**
     *
     * @param version entity version
     */
    public void setEntityVersion(int version);


    /**
     * Not persistend on database used to reference tags with the
     * HyperIoTAssetTagManager
     *
     * @return categoryIds
     */
    long[] getTagIds();

    /**
     * @param tagIds tagIds
     */
    void setTagIds(long[] tagIds);

    /**
     * Get the parent entity that contain this one
     *
     * @return The parent entity instance.
     */
    //HyperIoTBaseEntity getParent();

    /**
     * Get the system server API class name of this entity.
     *
     * @return The class name including the package name.
     */
    String getSystemApiClassName();
}
