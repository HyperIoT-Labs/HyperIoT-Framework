/*
 * Copyright 2019-2023 ACSoftware
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

package it.acsoftware.hyperiot.jobscheduler.repository;

import it.acsoftware.hyperiot.jobscheduler.api.JobSchedulerRepository;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author Aristide Cittadino Implementation class of the JobScheduler. This
 * class is used to interact with the persistence layer.
 * This is not related to HyperIoT Entity, it is used just for interacting with quartz dabatase
 */
@Component(service = JobSchedulerRepository.class, immediate = true)
public class JobSchedulerRepositoryImpl implements JobSchedulerRepository {
    public static final String QUARTZ_CREATION_SQL_POSTGRES_FILE = "quartz_creation_postgres.sql";
    Logger logger = LoggerFactory.getLogger(JobSchedulerRepositoryImpl.class);
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Reference(target = "(osgi.unit.name=hyperiot-jobScheduler-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        this.jpa = jpa;
    }

    @Override
    public void createQuartzTableIfNotExists(String filePath) {
        jpa.tx(entityManager -> {
            try {
                InputStream is;
                if (filePath == null)
                    is = this.getClass().getClassLoader().getResourceAsStream(QUARTZ_CREATION_SQL_POSTGRES_FILE);
                else
                    is = new FileInputStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String sql = br.lines().collect(Collectors.joining("\n"));
                Query q = entityManager.createNativeQuery(sql);
                q.executeUpdate();
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        });
    }
}
