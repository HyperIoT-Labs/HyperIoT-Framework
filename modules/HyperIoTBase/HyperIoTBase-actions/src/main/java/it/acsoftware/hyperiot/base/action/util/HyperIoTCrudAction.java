package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Class that enumerate all CRUD base actions. Note
 * that all base actions are used by entities in the HyperIoT platform.
 */
public enum HyperIoTCrudAction implements HyperIoTActionName {
    SAVE(Names.SAVE), UPDATE(Names.UPDATE), FIND(Names.FIND), FINDALL(Names.FINDALL), REMOVE(Names.REMOVE);
    public String name;

    public class Names {
        public static final String SAVE = "save";
        public static final String UPDATE = "update";
        public static final String FIND = "find";
        public static final String FINDALL = "find-all";
        public static final String REMOVE = "remove";
    }

    HyperIoTCrudAction(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
