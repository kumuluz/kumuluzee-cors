# KumuluzEE CORS
[![KumuluzEE CI](https://github.com/kumuluz/kumuluzee-cors/actions/workflows/kumuluzee-ci.yml/badge.svg)](https://github.com/kumuluz/kumuluzee-cors/actions/workflows/kumuluzee-ci.yml)

> KumuluzEE CORS filter extension for the KumuluzEE microservice framework. 

KumuluzEE CORS project is CORS filter extension for the KumuluzEE microservice framework. It enables fitting of Cross-Origin Resource 
Sharing support to Java web applications. 
 
CORS supports:

 * **Browser script perspective**: Allowing cross-domain requests, which are subject to tighter controls on the types of data that is exchanged.
  Cookies, for instance, are blocked unless specifically requested by the XHR author and allowed by the cross-domain web-service.
 * **Web service perspective**: By utilising the origin URL reported by the browser the target cross-domain web service can determine, based 
 on its origin policy, whether to allow or deny the request.
 
 
CORS specification is available at [CORS](https://www.w3.org/TR/cors/).
## Usage

You can enable the KumuluzEE CORS filter by adding the following dependency:
```xml
<dependency>
    <groupId>com.kumuluz.ee.cors</groupId>
    <artifactId>kumuluzee-cors</artifactId>
    <version>${kumuluzee-cors.version}</version>
</dependency>
```

### CORS filter configuration

When kumuluzee-cors dependency is included in the project, CORS filter can be configured in two ways: using **@CrossOrigin** annotation 
or using **servlet filter** by providing cors-filter configuration section in preferred config source (i.e. config.yaml, etcd etc.).


#### @CrossOrigin annotation

Annotation can be put on the following classes of the JAX-RS application: 
* Application class (with @ApplicationPath annotation)
* Resource classes (classes with annotation @Path)
* Resource operations

If @CrossOrigin annotation is provided on the Application class, all resources inherit the Cross Origin configuration. Configuration can 
be overridden by applying @CrossOrigin annotation on the resource class or method of the resource class.

If @CrossOrigin annotation is not provided on the Application class, only resource classes or methods with @CrossOrigin annotation have CORS Filter 
enabled.


##### @CrossOrigin annotation parameters

 * **name** default: ""
 
 * **allowGenericHttpRequests** default: *true*

 * **allowOrigin** default: "*"

 * **allowSubdomains** default: *false*

 * **supportedMethods** default: *"GET, POST, HEAD, OPTIONS"*

 * **supportedHeaders** default: "*"

 * **exposedHeaders** default: ""

 * **supportsCredentials** default: *true*

 * **maxAge** default: *-1* (int)

 * **tagRequests** default: *false*


Configuration for each annotation can be provided through config framework. Configuration for annotations is provided in namespace ```kumuluzee.cors-filter.annotations.<annotation-name>```. 
Parameter keys are the same as in annotation, except in kebab-case.

Example of annotation configuration in config:
```yaml
kumuluzee:
  cors-filter:
    annotations:
      my-resource:
        allow-origin: "https://kumuluz.com"
        allow-subdomains: false
```

Configuration in config takes priority over values defined in CrossOrigin annotation. To load the annotation configuration from config the key of the config must be the same as *name* parameter if name parameter is provided.
If name parameter is not provided in annotation config key must equal to the name of the class, if annotation is used on a class, or equal to the <name of class>-<name of method>, if annotation is used on method. 
If multiple methods share the same name in the same class, annotations must have *name* provided to distinguish between them.

Example of ***named*** CrossOrigin annotation:

```java
@RequestScoped
@Path("/myresource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@CrossOrigin(name = "my-resource", allowOrigin = "https://my.origin.com")
public class MyResource {
}
```

##### Example of using @CrossOrigin annotation

**Application class**

```java
@ApplicationPath("/v1")
@CrossOrigin
public class MyApplication extends Application {
}
```

**Resource class and method**

```java
@RequestScoped
@Path("/myresource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@CrossOrigin(allowOrigin = "https://my.origin.com")
public class MyResource {
    ...
    
    @GET
    @Path("/test")
    @CrossOrigin(allowOrigin="https://my-test.origin.com")
    public Response test() {
        ...
    }
```

#### CORS servlet filter

CORS servlet filter is enabled by providing cors-filter section in the config source. (**!Important**: If @CrossOrigin annotation is used in the application, servlet filter will not be initialized, annotations will take priority).
 
The following configuration parameters can be set for CORS filter:

  * **allow-generic-http-requests** {true|false} defaults to true.
  
    If true generic HTTP requests will be allowed to pass through the filter, else only valid and accepted CORS requests will be allowed (strict CORS filtering).
  
  
  * **allow-origin** {"*"|origin-list} defaults to *.
  
    Whitespace-separated list of origins that the CORS filter must allow. Requests from origins not included here will be refused with an HTTP 403 "Forbidden" response. If set to * (asterisk) any origin will be allowed.
    
    
  * **allow-subdomains** {true|false} defaults to false.
  
    If true the CORS filter will allow requests from any origin which is a subdomain origin of the allowed origins. A subdomain is matched by comparing its scheme and suffix (host name / IP address and optional port number).
    
    
  * **supported-methods** {method-list} defaults to "GET, POST, HEAD, OPTIONS".
  
    List of the supported HTTP methods. These are advertised through the Access-Control-Allow-Methods header and must also be implemented by the actual CORS web service. Requests for methods not included here will be refused by the CORS filter with an HTTP 405 "Method not allowed" response.
    
    
  * **supported-headers** {"*"|header-list} defaults to *.
  
    The names of the supported author request headers. These are advertised through the Access-Control-Allow-Headers header.
    If the configuration property value is set to * (asterisk) any author request header will be allowed. The CORS Filter implements this by simply echoing the requested value back to the browser.
    
    What is an author request header? This any custom header set by the browser JavaScript application through the XMLHttpRequest.setRequestHeader() method.
    
    
  * **exposed-headers** {header-list} defaults to empty list.
  
    List of the response headers other than simple response headers that the browser should expose to the author of the cross-domain request through the XMLHttpRequest.getResponseHeader() method. The CORS filter supplies this information through the Access-Control-Expose-Headers header.
     
                    
  * **supports-credentials** {true|false} defaults to true.
  
    Indicates whether user credentials, such as cookies, HTTP authentication or client-side certificates, are supported. The CORS filter uses this value in constructing the Access-Control-Allow-Credentials header.
    
    
  * **max-age** {int} defaults to -1 (unspecified).
  
    Indicates how long the results of a preflight request can be cached by the web browser, in seconds. If -1 unspecified. This information is passed to the browser via the Access-Control-Max-Age header.


  * **tag-requests** {true|false} defaults to false (no tagging).
  
    Enables HTTP servlet request tagging to provide CORS information to downstream handlers (filters and/or servlets).
  

Example CORS filter configuration (config.yaml):
```yaml
kumuluzee:
  cors-filter:
    servlet:
      enabled: true
      allow-generic-http-requests: false
      allow-origin: "*"
```

CORS servlet filter should be used for Servlet applications which don't use @WebServlet annotations. It can also be used for the JAX-RS applications, but no @CrossOrigin annotation should be used.

Remember to restart your web application or server after changing the CORS configuration!

##### Disabling CORS Servlet filter

To disable CORS servlet filter, simply set `kumuluzee.cors-filter.servlet.enabled`to `false`.

## Changelog

Recent changes can be viewed on GitHub on the [Releases Page](https://github.com/kumuluz/kumuluzee-cors/releases)


## Contribute

See the [contributing docs](https://github.com/kumuluz/kumuluzee-cors/blob/master/CONTRIBUTING.md)

When submitting an issue, please follow the 
[guidelines](https://github.com/kumuluz/kumuluzee-cors/blob/master/CONTRIBUTING.md#bugs).

When submitting a bugfix, write a test that exposes the bug and fails before applying your fix. Submit the test 
alongside the fix.

When submitting a new feature, add tests that cover the feature.


## License

MIT
