package it.acsoftware.hyperiot.contentrepository.service;

public class ContentRepositoryUtils {

    public static final String NT_FILE = "nt:file";

    public static final String NT_RESOURCE = "nt:resource";

    public static final String NT_FOLDER= "nt:folder";

    public static final String JCR_DATA = "jcr:data";

    public static final String JCR_CONTENT= "jcr:content";

    public static String getResourceRootFolderPath(String resourceClassName,long resourceId){
        return String.format("%s_%s", resourceClassName,resourceId);
    }
}
