package it.acsoftware.hyperiot.contentrepository.model;

public enum DocumentResourceType {


    FOLDER_TYPE(Names.FOLDER_TYPE),
    FILE_TYPE(Names.FILE_TYPE);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the ContentRepository  action
     */
    private DocumentResourceType(String name) {
        this.name = name;
    }

    /**
     * Gets the name of ContentRepository action
     */
    public String getName() {
        return name;
    }

    public static class Names {

        private static final String FOLDER_TYPE = "FOLDER_TYPE";

        private static final String FILE_TYPE = "FILE_TYPE";

        private Names() {
            throw new IllegalStateException("Utility class");
        }
    }
}
