package it.acsoftware.hyperiot.base.api;

public interface HyperIoTAssetTagManager {
    public void addAssetTag(String resourceName, long resourceId, long tagId);

    public void addAssetTags(String resourceName, long resourceId, long[] tagsId);

    /**
     * Find all tag ids belonging to a particular entity
     * @param resourceName Entity resource name
     * @param resourceId Entity resource id
     * @return tag ids
     */
    long[] findAssetTags(String resourceName, long resourceId);

    public void removeAssetTag(String resourceName, long resourceId, long tagId);

    public void removeAssetTags(String resourceName, long resourceId, long[] tagsId);
}
