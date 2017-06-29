## Spring-boot with Bucket4j and Hazelcast Demo
 
The aim of this demo is to demonstrate **rate-limiting** by using [Bucket4j](https://github.com/vladimir-bukhtoyarov/bucket4j)
 and the *In-Memory Data Grid* [Hazlecast](https://hazelcast.org/).
  
To do that, we will run two instances of the same application, they share the same bucket which is stored on Hazelcast. 
When a request from the same Ip hit any of the two instances the shared bucket is decremented. 

The result is immediately visible to any instance.
  
 ## Demo
 
 1. Run the first instance : ``mvn spring-boot:run -Dserver.port=8081``
 
 2. Run the second instance : ``mvn spring-boot:run -Dserver.port=8082``
 
 3. You can also run a third instance if you want.
 
 ## Testing
 
 When calling any of the two instances, say we run the following command ``curl -s -D - localhost:8081``, 
 the response looks like :
 
     HTTP/1.1 200
     X-Rate-Limit-Remaining: 9
     Content-Type: text/plain;charset=UTF-8
     Content-Length: 13
     Date: Thu, 29 Jun 2017 21:25:00 GMT
     
     Hello World !
     
 Notice the ``X-Rate-Limit-Remaining`` that indicates the remaining requests.
 
 In this sample we configured the bucket with a capacity of ``10 requests every minute`` as the following code shows.
 
 ```java
Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .buildConfiguration();
```
 Run alternatively the following commands :
 
 1. Request the instance one : ``curl -s -D - localhost:8081``

 2. Request the instance two : ``curl -s -D - localhost:8082``
 
 You will see the value of ``X-Rate-Limit-Remaining`` header decrementing
 until it equals ``0`` the subsequent request gives a ``429 HTTP status code`` along with message ``too many requests``.
 
     HTTP/1.1 429
     Content-Type: text/plain;charset=ISO-8859-1
     Content-Length: 17
     Date: Thu, 29 Jun 2017 21:36:11 GMT
     
     Too many requests
     
  **Note** : 
  
 > It is possible to not see the ``X-Rate-Limit-Remaining`` decrementing or on contrary incrementing, especially if you take a 
  long time to execute the queries, this is because *bucket4j* regenerates tokens immediately (for more detailed information
  read the documentation of [Refill.java](https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/bucket4j-core/src/main/java/io/github/bucket4j/Refill.java#L47) ) 