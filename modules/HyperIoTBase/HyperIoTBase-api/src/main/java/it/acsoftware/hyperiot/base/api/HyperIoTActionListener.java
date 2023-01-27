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
 * Author Aristide Cittadino
 * This class rapresents the basic ancestor of all hyperiot pre/post actions.
 * No method implemented in this way subclasses che define specific operations passing one or more HyperIoT Resources.
 */
public interface HyperIoTActionListener<T extends HyperIoTResource> {
}
