package it.acsoftware.hyperiot.base.model.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author Aristide Cittadino Model class for JWT Token
 */
public class JWTLoginResponse {
    private static Logger log = LoggerFactory.getLogger(JWTLoginResponse.class);
    private static ObjectMapper mapper = new ObjectMapper();
    /**
     * Serialized and encrypted string for JWT Token
     */
    private String token;
    /**
     * User info
     */
    private HyperIoTAuthenticable authenticable;

    /**
     * HashMap which stores resourcenames and profiles, so permissions
     */
    private HashMap<String, JWTProfile> profile;


    public JWTLoginResponse(String token, HyperIoTAuthenticable authenticable) {
        super();
        this.token = token;
        this.profile = new HashMap<>();
        this.authenticable = authenticable;
    }

    /**
     * @return the encoded Token
     */
    public String getToken() {
        return token;
    }

    public HashMap<String, JWTProfile> getProfile() {
        return profile;
    }

    public HyperIoTAuthenticable getAuthenticable() {
        return authenticable;
    }

    public void setAuthenticable(HyperIoTAuthenticable authenticable) {
        this.authenticable = authenticable;
    }

    public String toJson() {
        try {
            return mapper.writerWithView(HyperIoTJSONView.Public.class).writeValueAsString(this);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }

}
