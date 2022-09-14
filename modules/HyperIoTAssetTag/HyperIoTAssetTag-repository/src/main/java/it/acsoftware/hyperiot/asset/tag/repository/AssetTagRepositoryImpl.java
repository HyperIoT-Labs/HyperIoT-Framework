package it.acsoftware.hyperiot.asset.tag.repository;

import it.acsoftware.hyperiot.asset.tag.api.AssetTagRepository;
import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.api.HyperIoTAssetTagManager;
import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.Query;
import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of the AssetTag. This class
 * is used to interact with the persistence layer.
 */
@Component(service = {AssetTagRepository.class, HyperIoTAssetTagManager.class}, immediate = true)
public class AssetTagRepositoryImpl extends HyperIoTBaseRepositoryImpl<AssetTag>
        implements AssetTagRepository, HyperIoTAssetTagManager {
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Constructor for a AssetTagRepositoryImpl
     */
    public AssetTagRepositoryImpl() {
        super(AssetTag.class);
    }

    /**
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
    @Reference(target = "(osgi.unit.name=hyperiot-assetTag-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug( "invoking setJpa, setting: {}" , jpa);
        this.jpa = jpa;
    }

    /**
     * Add a tag on a resource
     */
    @Override
    public void addAssetTag(String resourceName, long resourceId, long tagId) {
        AssetTag tag = this.find(tagId,null);
        AssetTagResource acr = new AssetTagResource();
        acr.setTag(tag);
        acr.setResourceId(resourceId);
        acr.setResourceName(resourceName);
        tag.getResources().add(acr);
        this.update(tag);

    }

    /**
     * Add multiple tags on a resource
     */
    @Override
    public void addAssetTags(String resourceName, long resourceId, long[] tagsId) {
        for (int i = 0; i < tagsId.length; i++) {
            AssetTag tag = this.find(tagsId[i], null);
            AssetTagResource atr = new AssetTagResource();
            atr.setTag(tag);
            atr.setResourceId(resourceId);
            atr.setResourceName(resourceName);
            tag.getResources().add(atr);
            this.update(tag);
        }

    }

    @Override
    public long[] findAssetTags(String resourceName, long resourceId) {
        getLog().debug(
                "invoking findAssetTags for resource: {} - {}", new Object[]{ resourceName, resourceId });
        List<AssetTagResource> assetTagResourceList =  jpa.txExpr(entityManager -> {
            Query q = entityManager.createQuery(
                    "from AssetTagResource res where res.resourceName = :resourceName and res.resourceId = :resourceId",
                    AssetTagResource.class);
            q.setParameter("resourceName", resourceName);
            q.setParameter("resourceId", resourceId);
            return (List<AssetTagResource>) q.getResultList();
        });
        return assetTagResourceList
                .stream()
                .map(assetTagResource -> assetTagResource.getTag().getId())
                .mapToLong(Long::longValue)
                .toArray();
    }

    /**
     * Remove a tag from a resource
     */
    @Override
    public void removeAssetTag(String resourceName, long resourceId, long tagId) {
        AssetTag tag = this.find(tagId, null);
        AssetTagResource atr = this.findAssetTagResource(resourceName, resourceId, tagId);
        tag.getResources().remove(atr);
        this.update(tag);

    }

    /**
     * Remove multiple tags on a resource
     */
    @Override
    public void removeAssetTags(String resourceName, long resourceId, long[] tagsId) {
        for (int i = 0; i < tagsId.length; i++) {
            AssetTag tag = this.find(tagsId[i], null);
            AssetTagResource atr = this.findAssetTagResource(resourceName, resourceId, tag.getId());
            tag.getResources().remove(atr);
            this.update(tag);
        }

    }

    /**
     * Find a resource associated a tag
     */
    @Override
    public AssetTagResource findAssetTagResource(String resourceName, long resourceId, long tagId) {
        return jpa.txExpr(entityManager -> {
            Query q = entityManager.createQuery(
                    "from AssetTagResource res where res.tag.id=:tagId and res.resourceName = :resourceName and res.resourceId = :resourceId",
                    AssetTagResource.class);
            q.setParameter("tagId", tagId);
            q.setParameter("resourceName", resourceName);
            q.setParameter("resourceId", resourceId);
            return (AssetTagResource) q.getSingleResult();
        });
    }

    @Override
    public List<AssetTagResource> getAssetTagResourceList(String resourceName, long resourceId) {
        return jpa.txExpr(entityManager -> {
            Query q = entityManager.createQuery(
                    "from AssetTagResource res where res.resourceName = :resourceName and res.resourceId = :resourceId",
                    AssetTagResource.class);
            q.setParameter("resourceName", resourceName);
            q.setParameter("resourceId", resourceId);
            return (List<AssetTagResource>) q.getResultList();
        });
    }
}
