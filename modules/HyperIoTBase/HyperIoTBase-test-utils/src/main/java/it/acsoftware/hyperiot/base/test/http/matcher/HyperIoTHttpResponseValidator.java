package it.acsoftware.hyperiot.base.test.http.matcher;

import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Francesco Salerno
 * This class represent a model of a validator for HyperIoTHttpResponse.
 */
public class HyperIoTHttpResponseValidator {

    private List<HyperIoTHttpResponseValidationCriteria> validationCriteriaList;

    private static Logger log = LoggerFactory.getLogger(HyperIoTHttpResponseValidator.class.getName());

    private HyperIoTHttpResponseValidator(){

    }

    static HyperIoTHttpResponseValidator hyperIoTHttpResponseValidator(List<HyperIoTHttpResponseValidationCriteria> criteriaList){
        HyperIoTHttpResponseValidator validator = new HyperIoTHttpResponseValidator();
        validator.validationCriteriaList = new ArrayList<>();
        validator.validationCriteriaList.addAll(criteriaList);
        return  validator;
    }

    /**
     * @param response the response subject to validation
     * @return true if the response is valid according to the criteria.
     */
    public boolean validateResponse(HyperIoTHttpResponse response){
        for( HyperIoTHttpResponseValidationCriteria criteria : validationCriteriaList){
            if ( ! criteria.validate(response)){
                log.debug("HyperIoTHttpResponseValidation fail. \n " +
                        "Response status : "+ response.getResponseStatus() + " \n"+
                        "Response body is : "+ response.getResponseBody()+ " \n"+
                        "HyperIoTHttpResponseValidationCriteria class name is : " + criteria.getClass().getName()+ " . ");
                return false;
            }
        }
        return true;
    }



}
