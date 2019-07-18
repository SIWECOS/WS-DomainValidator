# WS-DomainValidator
WS-DomainValidator is a webservice created by the Chair for Network and Data Security from the Ruhr-University Bochum for the SIWECOS Project. The Webservice checks a provided URL syntactical correctnes, extracts the domain, extracts possible HTTP redirects and retrieves the DNS MX-Records for the responsible mail server domains.

# Compiling
In order to compile and use WS-DomainValidator, you need to have java (OpenJDK 8) installed, as well as maven.

```bash
$ cd WS-DomainValidator
$ mvn clean package

```

# Running
In order to run WS-DomainValidator you need to deploy the .war file from the target/ folder to your favourite java application server (eg. Glassfish, Tomcat ...). After that the webservice should be up and running and can be called by sending a POST like
```
{
  "domain": "https://google.de",
  "crawl": true,
  "maxCount": 10,
  "maxDepth": 2,
  "userAgent": "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0",
  "allowSubdomains": true
  
}
```
to
```
http://127.0.0.1:8080/WS-HostValidator/validate
```

or 

```
http://127.0.0.1:8080/validate
```
Depending on your application server.

# Results
The webservice will do various sanity checks and dns queries and will then directy return the result like this: 
An example output may look like this:
```json
{
    "name": "Validator",
    "hasError": false,
    "domain": "google.de",
    "originalUrl": "http://google.de",
    "urlToScan": "http://google.de",
    "urlIsSyntacticalOk": true,
    "dnsResolves": true,
    "httpRedirect": false,
    "mailServerDomainList": [
        "aspmx.l.google.com.",
        "alt3.aspmx.l.google.com.",
        "alt2.aspmx.l.google.com.",
        "alt4.aspmx.l.google.com.",
        "alt1.aspmx.l.google.com."
    ]
}
```



# Docker
You can also run WS-HostValidator with Docker. You can build with:
```
docker build . -t validator
```
You can then run it with:
```
docker run -it --network host validator
```
The webservice is then reachable under:
```
http://127.0.0.1:8080/validate
```
