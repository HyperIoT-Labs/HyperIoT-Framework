package it.acsoftware.hyperiot.mail.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate Mail Actions
 */
public enum HyperIoTMailAction implements HyperIoTActionName {

    //TO DO: add enumerations here
    SEND_EMAIL(Names.SEND_EMAIL),
    GENERATE_TEXT_FROM_TEMPLATE(Names.GENERATE_TEXT_FROM_TEMPLATE);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the Mail  action
     */
    private HyperIoTMailAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of Mail action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String SEND_EMAIL = "send_email";
        public static final String GENERATE_TEXT_FROM_TEMPLATE = "generate_text_from_template";
    }

}
