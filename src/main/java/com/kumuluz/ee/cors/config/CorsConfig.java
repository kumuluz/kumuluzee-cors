/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.cors.config;

/**
 * CorsConfig class.
 *
 * @author Zvone Gazvoda
 * @since 1.0.0
 */
public class CorsConfig {

    private Boolean allowGenericHttpRequests;
    private String allowOrigin;
    private Boolean allowSubdomains;
    private String supportedMethods;
    private String supportedHeaders;
    private String exposedHeaders;
    private Boolean supportsCredentials;
    private Integer maxAge;
    private Boolean tagRequests;
    private String pathSpec;

    public Boolean getAllowGenericHttpRequests() {
        return allowGenericHttpRequests;
    }

    public void setAllowGenericHttpRequests(String allowGenericHttpRequestsLocal) {
        Boolean allowGenericHttpRequests = true;

        if (allowGenericHttpRequestsLocal.equals("false")) {

            allowGenericHttpRequests = false;

        }

        this.allowGenericHttpRequests = allowGenericHttpRequests;
    }

    public String getAllowOrigin() {
        return allowOrigin;
    }

    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    public Boolean getAllowSubdomains() {
        return allowSubdomains;
    }

    public void setAllowSubdomains(String allowSubdomainsLocal) {
        Boolean allowSubdomains = false;

        if (allowSubdomainsLocal.equals("true")) {

            allowSubdomains = true;

        }

        this.allowSubdomains = allowSubdomains;
    }

    public String getSupportedMethods() {
        return supportedMethods;
    }

    public void setSupportedMethods(String supportedMethods) {
        this.supportedMethods = supportedMethods;
    }

    public String getSupportedHeaders() {
        return supportedHeaders;
    }

    public void setSupportedHeaders(String supportedHeaders) {
        this.supportedHeaders = supportedHeaders;
    }

    public String getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public Boolean getSupportsCredentials() {
        return supportsCredentials;
    }

    public void setSupportsCredentials(String supportsCredentialsLocal) {
        Boolean supportsCredentials = true;

        if (supportsCredentialsLocal.equals("false")) {

            supportsCredentials = false;

        }

        this.supportsCredentials = supportsCredentials;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = Integer.parseInt(maxAge);
    }

    public Boolean getTagRequests() {
        return tagRequests;
    }

    public void setTagRequests(String tagRequestsLocal) {

        Boolean tagRequests = false;

        if (tagRequestsLocal.equals("true")) {

            tagRequests = true;

        }

        this.tagRequests = tagRequests;
    }

    public String getPathSpec() {
        return pathSpec;
    }

    public void setPathSpec(String pathSpec) {
        this.pathSpec = pathSpec;
    }

    public CorsConfig applyPermitDefaultValues() {

        this.allowGenericHttpRequests = true;
        this.allowOrigin = "*";
        this.allowSubdomains = false;
        this.supportedMethods = "GET, POST, HEAD, OPTIONS";
        this.supportedHeaders = "*";
        this.exposedHeaders = "";
        this.supportsCredentials = true;
        this.maxAge = -1;
        this.tagRequests = false;

        return this;
    }
}
