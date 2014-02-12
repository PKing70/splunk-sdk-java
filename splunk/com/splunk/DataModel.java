/*
 * Copyright 2014 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk;

import java.util.Collection;

/**
 * DataModel represents a data model on the server. Data models contain
 * data model objects, which specify structured views on Splunk data.
 */
public class DataModel extends Entity {
    private Collection<DataModelObject> objects;

    public DataModel(Service service, String path) {
        super(service, path);
    }

    public Collection<DataModelObject> getObjects() {
        return objects;
    }
}
