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

package it.acsoftware.hyperiot.query.util.filter.operators;


import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;

/**
 * This class maps the concept of generic binary operator like and,or...
 */
public abstract class HyperIoTQueryBinaryLogicOperator extends HyperIoTQueryLogicOperator {
    private HyperIoTQuery leftOp;
    private HyperIoTQuery rightOp;

    public HyperIoTQueryBinaryLogicOperator(HyperIoTQuery leftOp, HyperIoTQuery rightOp) {
        this.leftOp = leftOp;
        this.rightOp = rightOp;
    }

    public HyperIoTQuery getLeftOp() {
        return leftOp;
    }

    public HyperIoTQuery getRightOp() {
        return rightOp;
    }
}
