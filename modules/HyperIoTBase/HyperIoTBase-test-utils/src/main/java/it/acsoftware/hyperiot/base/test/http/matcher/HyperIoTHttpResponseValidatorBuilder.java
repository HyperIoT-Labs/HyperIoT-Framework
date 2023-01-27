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

package it.acsoftware.hyperiot.base.test.http.matcher;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Salerno
 *
 * This class represent a Builder of an HyperIoTHttpResponseValidator.
 */
public class HyperIoTHttpResponseValidatorBuilder {

    private List<HyperIoTHttpResponseValidationCriteria> criteriaList;

    private HyperIoTHttpResponseValidatorBuilder(){

    }

    /**
     * Factory method to start the build process.
     * @return an HyperIoTHttpResponseValidatorBuilder
     */
    public static HyperIoTHttpResponseValidatorBuilder validatorBuilder(){
        HyperIoTHttpResponseValidatorBuilder builder = new HyperIoTHttpResponseValidatorBuilder();
        builder.criteriaList = new ArrayList<>();
        return builder;
    }

    /**
     * @param validationCriteria a custom validation criteria.
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public  HyperIoTHttpResponseValidatorBuilder withCustomCriteria(HyperIoTHttpResponseValidationCriteria validationCriteria){
        this.criteriaList.add(validationCriteria);
        return this;
    }

    /**
     * @param propertiesName the exact list of property name that we expected is present in response body.
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder containExactProperties(List<String> propertiesName){
        this.criteriaList.add(HyperIoTHttpResponseContainExactProperties.factory(propertiesName));
        return this;
    }

    /**
     * @param propertiesName list of property name that we expected is present in response body.
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder containProperties(List<String> propertiesName){
        this.criteriaList.add(HyperIoTHttpResponseContainProperties.factory(propertiesName));
        return this;
    }

    /**
     * @param innerPropertyName the inner property field (Separate properties name by dot to specify property gerarchy path)
     * @param innerPropertyField the field related to the inner property.
     *  @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder containInnerProperties(String innerPropertyName , List<String> innerPropertyField){
        this.criteriaList.add(HyperIoTHttpResponseContainInnerProperties.factory(innerPropertyName, innerPropertyField));
        return this;
    }

    /**
     * @param innerPropertyName the inner property field (Separate properties name by dot to specify property gerarchy path)
     * @param innerPropertyField the field related to the inner property.
     *  @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder containExactInnerProperties(String innerPropertyName, List<String> innerPropertyField){
        this.criteriaList.add(HyperIoTHttpResponseContainExactInnerProperties.factory(innerPropertyName, innerPropertyField));
        return this;
    }

    /**
     * This validator verify if the body on the response is valid respect to the json schema generate from entity class parameter.
     * @param entityClass the entityClass from which generate the schema.
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder matchEntitySchema(Class<? extends HyperIoTBaseEntity> entityClass){
        this.criteriaList.add(HyperIoTHttpResponseJsonSchemaMatcher.factory(entityClass));
        return this;
    }

    public HyperIoTHttpResponseValidatorBuilder containHyperIoTAbstractEntityFields(){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("id");
        hyperIoTAbstractEntityFields.add("entityCreateDate");
        hyperIoTAbstractEntityFields.add("entityModifyDate");
        hyperIoTAbstractEntityFields.add("categoryIds");
        hyperIoTAbstractEntityFields.add("tagIds");
        this.criteriaList.add(HyperIoTHttpResponseContainProperties.factory(hyperIoTAbstractEntityFields));
        return this;
    }

    public HyperIoTHttpResponseValidatorBuilder containHyperIoTAbstractEntityFields(String innerProperty){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("id");
        hyperIoTAbstractEntityFields.add("entityCreateDate");
        hyperIoTAbstractEntityFields.add("entityModifyDate");
        hyperIoTAbstractEntityFields.add("categoryIds");
        hyperIoTAbstractEntityFields.add("tagIds");
        hyperIoTAbstractEntityFields.add("entityVersion");
        this.criteriaList.add(HyperIoTHttpResponseContainInnerProperties.factory(innerProperty,hyperIoTAbstractEntityFields));
        return this;
    }

    /**
     * This validator verify if the HyperIoTHttpResponse contain the exact property defined in HyperIoTPaginatedResult.
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder containExactHyperIoTPaginatedProperties(){
        List<String> hyperIoTPaginatedProperties = new ArrayList<>();
        hyperIoTPaginatedProperties.add("currentPage");
        hyperIoTPaginatedProperties.add("numPages");
        hyperIoTPaginatedProperties.add("nextPage");
        hyperIoTPaginatedProperties.add("delta");
        hyperIoTPaginatedProperties.add("results");
        this.criteriaList.add(HyperIoTHttpResponseContainExactProperties.factory(hyperIoTPaginatedProperties));
        return this;
    }

    /**
     * @param status used to validate HyperIoTHttpResponse's status
     * @return the current instance of HyperIoTHttpResponseValidatorBuilder
     */
    public HyperIoTHttpResponseValidatorBuilder withStatusEqual(int status){
        this.criteriaList.add(HyperIoTHttpResponseStatusValidationCriteria.factory(status));
        return this;
    }



    /**
     * Terminate the build process with the return of the HyperIoTHttpResponseValidator.
     * @return an HyperIoTHttpResponseValidator.
     */
    public HyperIoTHttpResponseValidator build(){
        return HyperIoTHttpResponseValidator.hyperIoTHttpResponseValidator(this.criteriaList);
    }

}
