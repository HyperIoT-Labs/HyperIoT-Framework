package it.acsoftware.hyperiot.contentrepository.model;

import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HyperIoTDocumentNode {

    @JsonView(HyperIoTJSONView.Public.class)
    private String documentResourceName;

    @JsonView(HyperIoTJSONView.Public.class)
    private DocumentResourceType documentResourceType;

    @JsonView(HyperIoTJSONView.Public.class)
    private List<HyperIoTDocumentNode> documentResourceChild;

    public HyperIoTDocumentNode(){
        this.documentResourceChild = new ArrayList<>();
    }


    public String getDocumentResourceName() {
        return documentResourceName;
    }

    public void setDocumentResourceName(String documentResourceName) {
        this.documentResourceName = documentResourceName;
    }

    public DocumentResourceType getDocumentResourceType() {
        return documentResourceType;
    }

    public void setDocumentResourceType(DocumentResourceType documentResourceType) {
        this.documentResourceType = documentResourceType;
    }

    public List<HyperIoTDocumentNode> getDocumentResourceChild() {
        return documentResourceChild;
    }

    public void setDocumentResourceChild(List<HyperIoTDocumentNode> documentResourceChild) {
        this.documentResourceChild = documentResourceChild;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperIoTDocumentNode that = (HyperIoTDocumentNode) o;
        return Objects.equals(documentResourceName, that.documentResourceName) && documentResourceType == that.documentResourceType && Objects.equals(documentResourceChild, that.documentResourceChild);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentResourceName, documentResourceType, documentResourceChild);
    }
}
