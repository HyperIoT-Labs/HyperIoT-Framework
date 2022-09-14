package it.acsoftware.hyperiot.base.test.http.matcher;

import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

/**
 * @author Francesco Salerno
 * This interface represent a strategy to validate an HyperIoTHttpResponse.
 */
public interface HyperIoTHttpResponseValidationCriteria {
     /**
      *
      * @param response the HyperIoTHttpResponse subject to validation
      * @return true if the response is valid, according to the criteria.
      */
     boolean validate(HyperIoTHttpResponse response);

}
