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

package it.acsoftware.hyperiot.base.api;

/**
 * Interface which map the concept of an action which is going to be executed after another one on a particular entity
 * @param <T> Resource type
 */
public interface HyperIoTPreAction<T extends HyperIoTResource> extends HyperIoTActionListener{

    /**
     * Execute an action after another one
     * @param entity Entity on which the first action was executed
     */
    void execute(T entity);
}
