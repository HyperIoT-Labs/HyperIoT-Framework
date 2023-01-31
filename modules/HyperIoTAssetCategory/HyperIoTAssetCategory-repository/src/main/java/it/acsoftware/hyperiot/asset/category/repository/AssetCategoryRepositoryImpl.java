/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.asset.category.repository;

import it.acsoftware.hyperiot.asset.category.api.AssetCategoryRepository;
import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.asset.category.model.AssetCategoryResource;
import it.acsoftware.hyperiot.base.api.HyperIoTAssetCategoryManager;
import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.Query;
import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of the AssetCategory. This
 * class is used to interact with the persistence layer.
 */
@Component(service = {AssetCategoryRepository.class,
        HyperIoTAssetCategoryManager.class}, immediate = true)
public class AssetCategoryRepositoryImpl extends HyperIoTBaseRepositoryImpl<AssetCategory>
        implements AssetCategoryRepository, HyperIoTAssetCategoryManager {
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Constructor for a AssetCategoryRepositoryImpl
     */
    public AssetCategoryRepositoryImpl() {
        super(AssetCategory.class);
    }

    /**
     * @return The current jpaTemplate
     */
    @Override
    protected JpaTemplate getJpa() {
        getLog().debug( "invoking getJpa, returning: {}", jpa);
        return jpa;
    }

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Override
    @Reference(target = "(osgi.unit.name=hyperiot-assetCategory-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug( "invoking setJpa, setting: {}", jpa);
        this.jpa = jpa;
    }

    /**
     * Find the assect cagegory resource from the cateogry id
     */
    @Override
    public AssetCategoryResource findAssetCategoryResource(String resourceName, long resourceId,
                                                           long categoryId) {
        return jpa.txExpr(entityManager -> {
            Query q = entityManager.createQuery(
                    "from AssetCategoryResource res where res.category.id=:categoryId and res.resourceName = :resourceName and res.resourceId = :resourceId",
                    AssetCategoryResource.class);
            q.setParameter("categoryId", categoryId);
            q.setParameter("resourceName", resourceName);
            q.setParameter("resourceId", resourceId);
            return (AssetCategoryResource) q.getSingleResult();
        });
    }

    /**
     * Add category to a resources
     */
    @Override
    public void addAssetCategory(String resourceName, long resourceId, long categoryId) {
        getLog().debug(
                "invoking addAssetCategory for resource:  {} - {}", new Object[]{resourceName, resourceId});
        AssetCategory category = this.find(categoryId, null);
        AssetCategoryResource acr = new AssetCategoryResource();
        acr.setCategory(category);
        acr.setResourceId(resourceId);
        acr.setResourceName(resourceName);
        category.getResources().add(acr);
        this.update(category);
    }

    /**
     * Add multiple categories to a resource
     */
    @Override
    public void addAssetCategories(String resourceName, long resourceId, long[] categoriesId) {
        getLog().debug(
                "invoking addAssetCategories for resource: {} - {}", new Object[]{resourceName, resourceId});
        for (int i = 0; i < categoriesId.length; i++) {
            AssetCategory category = this.find(categoriesId[i], null);
            AssetCategoryResource acr = new AssetCategoryResource();
            acr.setCategory(category);
            acr.setResourceId(resourceId);
            acr.setResourceName(resourceName);
            category.getResources().add(acr);
            this.update(category);
        }
    }

    @Override
    public long[] findAssetCategories(String resourceName, long resourceId) {
        getLog().debug(
                "invoking findAssetCategories for resource: {} - {}", new Object[]{ resourceName, resourceId });
        List<AssetCategoryResource> assetCategoryResourceList =  jpa.txExpr(entityManager -> {
            Query q = entityManager.createQuery(
                    "from AssetCategoryResource res where res.resourceName = :resourceName and res.resourceId = :resourceId",
                    AssetCategoryResource.class);
            q.setParameter("resourceName", resourceName);
            q.setParameter("resourceId", resourceId);
            return (List<AssetCategoryResource>) q.getResultList();
        });
        return assetCategoryResourceList
                .stream()
                .map(assetCategoryResource -> assetCategoryResource.getCategory().getId())
                .mapToLong(Long::longValue)
                .toArray();
    }

    /**
     * Remove a category from a resource
     */
    @Override
    public void removeAssetCategory(String resourceName, long resourceId, long categoryId) {
        getLog().debug(
                "invoking removeAssetCategory for resource: {} - {}", new Object[]{resourceName, resourceId});
        AssetCategory category = this.find(categoryId, null);
        AssetCategoryResource acr = this.findAssetCategoryResource(resourceName, resourceId,
                categoryId);
        category.getResources().remove(acr);
        this.update(category);
    }

    /**
     * removes list of categories from a resource
     */
    @Override
    public void removeAssetCategories(String resourceName, long resourceId, long[] categoriesId) {
        getLog().debug(
                "invoking removeAssetCategories for resource: {} - {}", new Object[]{resourceName, resourceId});
        for (int i = 0; i < categoriesId.length; i++) {
            AssetCategory category = this.find(categoriesId[i], null);
            AssetCategoryResource acr = this.findAssetCategoryResource(resourceName, resourceId,
                    category.getId());
            category.getResources().remove(acr);
            this.update(category);
        }
    }

}
