package it.acsoftware.hyperiot.jobscheduler.api;

import java.io.File;

public interface JobSchedulerRepository{
    void createQuartzTableIfNotExists(String initScriptFilePath);
}
