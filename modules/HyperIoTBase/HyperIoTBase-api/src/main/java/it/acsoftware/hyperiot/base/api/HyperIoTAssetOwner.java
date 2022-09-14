package it.acsoftware.hyperiot.base.api;

/**
 * @author Aristide Cittadino
 * Interface which maps the concept of an entity which owns asset and categories
 */
public interface HyperIoTAssetOwner extends HyperIoTResource, HyperIoTOwnedResource {
    public String getOwnerResourceName();

    public Long getOwnerResourceId();
}
