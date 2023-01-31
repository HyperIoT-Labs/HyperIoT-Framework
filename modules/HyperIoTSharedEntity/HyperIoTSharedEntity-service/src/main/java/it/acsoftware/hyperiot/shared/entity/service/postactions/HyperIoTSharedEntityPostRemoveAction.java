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

package it.acsoftware.hyperiot.shared.entity.service.postactions;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPostRemoveAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.shared.entity.api.SharedEntitySystemApi;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = HyperIoTPostRemoveAction.class, property = {"type=it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity"})
public class HyperIoTSharedEntityPostRemoveAction<T extends HyperIoTBaseEntity> implements HyperIoTPostRemoveAction<T> {
    private static Logger log = LoggerFactory.getLogger(HyperIoTSharedEntityPostRemoveAction.class.getName());

    private SharedEntitySystemApi sharedEntitySystemService;

    /**
     *
     * @param sharedEntitySystemService SharedEntitySystemApi service
     */
    @Reference
    public void setSharedEntitySystemApi(SharedEntitySystemApi sharedEntitySystemService) {
        this.sharedEntitySystemService = sharedEntitySystemService;
    }

    @Override
    public void execute(T entity) {
        if (HyperIoTSharedEntity.class.isAssignableFrom(entity.getClass())) {
            log.debug( "Removing records with entityResourceName {} and entityId {} from SharedEntity table after deleting entity {}", new Object[]{entity.getResourceName(), entity.getId(), entity.getClass().getSimpleName()});

            List<SharedEntity> sharedEntityList = sharedEntitySystemService.findByEntity(entity.getResourceName(), entity.getId(), null, null);
            sharedEntityList.forEach(sharedEntity -> sharedEntitySystemService.removeByPK(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId(), sharedEntity.getUserId(), null));
        }
    }
}
