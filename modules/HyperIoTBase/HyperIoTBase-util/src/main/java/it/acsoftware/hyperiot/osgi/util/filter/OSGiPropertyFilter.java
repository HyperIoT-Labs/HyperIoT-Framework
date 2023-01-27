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

package it.acsoftware.hyperiot.osgi.util.filter;

/**
 * Author Aristide Cittadino
 * OSGi Filter Property filter
 */
public class OSGiPropertyFilter implements OSGiFilter {
    private String name;
    private String value;
    private boolean not;

    public OSGiPropertyFilter(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getFilter() {
        String filterCondition = "(" + this.getName() + "=" + getValue() + ")";
        if (!not)
            return filterCondition;
        else
            return "(!" + filterCondition + ")";
    }

    @Override
    public OSGiFilter and(OSGiFilter filter) {
        return new OSGiFilterAndCondition(this, filter);
    }

    @Override
    public OSGiFilter and(String propertyName, String propertyValue) {
        return new OSGiFilterAndCondition(this, new OSGiPropertyFilter(propertyName, propertyValue));
    }

    @Override
    public OSGiFilter or(OSGiFilter filter) {
        return new OSGiFilterOrCondition(this, filter);
    }

    @Override
    public OSGiFilter or(String propertyName, String propertyValue) {
        return new OSGiFilterOrCondition(this, new OSGiPropertyFilter(propertyName, propertyValue));
    }

    @Override
    public OSGiFilter not() {
        this.not = true;
        return this;
    }

}
