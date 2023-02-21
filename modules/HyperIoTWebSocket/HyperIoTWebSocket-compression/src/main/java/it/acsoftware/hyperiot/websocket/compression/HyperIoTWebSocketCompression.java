/*
 * Copyright 2019-2023 HyperIoT
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

package it.acsoftware.hyperiot.websocket.compression;

import org.eclipse.jetty.websocket.api.Session;

//TO DO: must implement compression policy
public abstract class HyperIoTWebSocketCompression {

    public abstract void init(Session s);

    public abstract void dispose(Session s);

    public abstract byte[] compress(byte[] message);

    public abstract byte[] decompress(byte[] message);
}
