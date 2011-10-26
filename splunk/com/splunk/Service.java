/*
 * Copyright 2011 Splunk, Inc.
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

// UNDONE: storage/passwords
// UNDONE: getAlerts
// UNDONE: getOutputs
// UNDONE: public Object parse(String query) {}
// UNDONE: public void restart() {}

package com.splunk;

import com.splunk.http.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class Service extends com.splunk.http.Service {
    protected String token = null;
    protected String namespace = null;
    private String prefix = null;

    public static String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 8089;
    public static String DEFAULT_SCHEME = "https";

    public Service(String host) {
        super(host);
    }

    public Service(String host, int port) {
        super(host, port);
    }

    public Service(String host, int port, String scheme) {
        super(host, port, scheme);
    }

    public Service(ServiceInfo info) {
        super();
        this.host = info.host == null ? DEFAULT_HOST : info.host;
        this.port = info.port == null ? DEFAULT_PORT : info.port;
        this.scheme = info.scheme == null ? DEFAULT_SCHEME : info.scheme;
        this.namespace = info.namespace;
    }

    // Ensures that the given path is fully qualified, prepending a
    // path prefix as necessarry.
    protected String fullpath(String path) {
        if (path.startsWith("/")) 
            return path;
        if (namespace == null)
            return "/services/" + path;
        return String.format("/servicesNS/%s/%s", namespace, path);
    }

    public EntityCollection<Application> getApplications() {
        return new EntityCollection<Application>(this, "apps/local", Application.class);
    }

    public EntityCollection<Entity> getConfigurations() {
        return null; // UNDONE
    }

    public List<String> getCapabilities() {
        Entity caps = Entity.read(this, "authorization/capabilities");
        return (List<String>)caps.getValue("capabilities");
    }

    public Entity getDeploymentClient() {
        return Entity.read(this, "deployment/client");
    }

    public EntityCollection<Entity> getDeploymentServers() {
        return new EntityCollection<Entity>(this, "deployment/server");
    }

    public EntityCollection<Entity> getDeploymentServerClasses() {
        return new EntityCollection<Entity>(this, "deployment/serverclass");
    }

    public EntityCollection<Entity> getDeploymentTenants() {
        return new EntityCollection<Entity>(this, "deployment/tenants");
    }

    public EntityCollection<Entity> getEventTypes() {
        return new EntityCollection<Entity>(this, "saved/eventtypes");
    }

    public EntityCollection<Index> getIndexes() {
        return new EntityCollection<Index>(this, "data/indexes", Index.class);
    }

    public Entity getInfo() {
        return Entity.read(this, "server/info");
    }

    public EntityCollection<Entity> getInputs() {
        return null; //  UNDONE: flatten?
    }

    public EntityCollection<Job> getJobs() {
        return new EntityCollection<Job>(this, "search/jobs", Job.class);
    }

    public EntityCollection<Entity> getLicenseGroups() {
        return new EntityCollection<Entity>(this, "licenser/groups");
    }

    public EntityCollection<Entity> getLicenseMessages() {
        return new EntityCollection<Entity>(this, "licenser/messages");
    }

    public EntityCollection<Entity> getLicensePools() {
        return new EntityCollection<Entity>(this, "licenser/pools");
    }

    public EntityCollection<Entity> getLicenseSlaves() {
        return new EntityCollection<Entity>(this, "licenser/slaves");
    }

    public EntityCollection<Entity> getLicenseStacks() {
        return new EntityCollection<Entity>(this, "licenser/stacks");
    }

    public EntityCollection<Entity> getLicenses() {
        return new EntityCollection<Entity>(this, "licenser/licenses");
    }

    public EntityCollection<Entity> getLoggers() {
        return new EntityCollection<Entity>(this, "server/logger");
    }

    public EntityCollection getMessages() {
        return new EntityCollection(this, "messages", Message.class);
    }

    public EntityCollection getPasswords() {
        // UNDONE: Figure out which version of the product this showed up in
        return new EntityCollection(this, "storage/passwords");
    }

    public EntityCollection<Entity> getRoles() {
        return new EntityCollection<Entity>(this, "authentication/roles");
    }

    public EntityCollection<Entity> getSearches() {
        return new EntityCollection<Entity>(this, "saved/searches");
    }

    public Object getSettings() {
        return null; // UNDONE
    }

    public EntityCollection<Entity> getUsers() {
        return new EntityCollection<Entity>(this, "authentication/users");
    }

    public Service login(String username, String password) {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("username", username);
        args.put("password", password);
        ResponseMessage response = super.post("/services/auth/login", args);
        // UNDONE: Check status
        String sessionKey = parseXml(response)
            .getElementsByTagName("sessionKey")
            .item(0)
            .getTextContent();
        this.token = "Splunk " + sessionKey;
        return this;
    }

    // Forget the session token
    public Service logout() {
        this.token = null;
        return this;
    }

    // Returns the response content as an XML DOM.
    // UNDONE: The following helper is only used by the login method, should
    // find a way to combine this with a similar helper in com.splunk.atom.
    public Document parseXml(ResponseMessage response) {
        try {
            InputStream content = response.getContent();
            DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new InputStreamReader(content));
            return builder.parse(inputSource);
        }
        catch (Exception e) {
            // UNDONE: SplunkException
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseMessage send(String path, RequestMessage request) {
        request.getHeader().put("Authorization", token);
        return super.send(fullpath(path), request);
    }
}

