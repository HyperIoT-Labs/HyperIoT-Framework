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

package it.acsoftware.hyperiot.base.test.util;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

import java.util.Collection;
import java.util.function.Function;

public class HyperIoTTestUtils {

    public static <T extends HyperIoTBaseEntity> void truncateTables(HyperIoTBaseRepository<T> baseRepository, Function<T, Boolean> filter) {
        Collection<T> allEntities = baseRepository.findAll(null);
        if (filter == null) {
            filter = (entity) -> {
                return true;
            };
        }
        final Function<T, Boolean> filterFunction = filter;
        allEntities.stream().filter(entity -> filterFunction.apply(entity)).forEach(toDelete ->
                baseRepository.remove(toDelete.getId())
        );
    }

    public static <T extends HyperIoTBaseEntity> void truncateTables(HyperIoTBaseEntitySystemApi<T> systemApi, Function<T, Boolean> filter) {
        Collection<T> allEntities = systemApi.findAll(null, null);
        if (filter == null) {
            filter = (entity) -> {
                return true;
            };
        }
        final Function<T, Boolean> filterFunction = filter;
        allEntities.stream().filter(entity -> filterFunction.apply(entity)).forEach(toDelete ->
                systemApi.remove(toDelete.getId(), null)
        );
    }
}
