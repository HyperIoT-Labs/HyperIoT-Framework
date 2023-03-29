/*
 * Copyright 2019-2023 HyperIoT
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
