package it.acsoftware.hyperiot.base.api;

public interface HyperIoTAssetCategoryManager {
    void addAssetCategory(String resourceName, long resourceId, long categoryId);

     void addAssetCategories(String resourceName, long resourceId, long[] categoriesId);

    /**
     * Find all category ids belonging to a particular entity
     * @param resourceName Entity resource name
     * @param resourceId Entity resource id
     * @return category ids
     */
    long[] findAssetCategories(String resourceName, long resourceId);

    void removeAssetCategory(String resourceName, long resourceId, long categoryId);

    void removeAssetCategories(String resourceName, long resourceId, long[] categoriesId);
}
