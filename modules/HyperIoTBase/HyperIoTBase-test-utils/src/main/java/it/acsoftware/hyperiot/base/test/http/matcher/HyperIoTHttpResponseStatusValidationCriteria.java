package it.acsoftware.hyperiot.base.test.http.matcher;

import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

/**
 * @author Francesco Salerno
 *
 * This class is an utility to validate HyperIoTHttpResponse status code.
 */
public class HyperIoTHttpResponseStatusValidationCriteria implements HyperIoTHttpResponseValidationCriteria{

    private int status;

    private HyperIoTHttpResponseStatusValidationCriteria(){

    }

    static HyperIoTHttpResponseStatusValidationCriteria factory(int status){
        HyperIoTHttpResponseStatusValidationCriteria criteria = new HyperIoTHttpResponseStatusValidationCriteria();
        criteria.status = status;
        return criteria;
    }


    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return response.getResponseStatus() == status ;
    }

}
