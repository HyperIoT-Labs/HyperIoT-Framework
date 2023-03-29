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

package it.acsoftware.hyperiot.base.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GenericExceptionMapperProvider implements ExceptionMapper<Throwable> {
    private static Logger log = LoggerFactory.getLogger(GenericExceptionMapperProvider.class.getName());

    class ErrorMessage {
        int status;
        String message;
        String developerMessage;

        public ErrorMessage(int status, String message) {
            this.status = status;
            String[] messages = message.split("\\|");
            this.message = messages[0];
            if (messages.length > 1) {
                this.developerMessage = messages[1];
            }
        }

        public int getStatus() {
            return this.status;
        }

        public String getMessage() {
            return this.message;
        }

        public String getDeveloperMessage() {
            return this.developerMessage;
        }
    }

    public Response toResponse(Throwable ex) {
        log.error( ex.getMessage(), ex);
        ErrorMessage errorMessage = new ErrorMessage(500,
                "An internal error has occurred|" + ex.getMessage());
        return Response.serverError().entity(errorMessage).type(MediaType.APPLICATION_JSON).build();

    }

}
