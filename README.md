# moneytransfer

### Purpose
The purpose of this task is to learn Vertx + RxJava usage. 
The task is develop the system, for transferring money between accounts. 

### Whats done
* initiation of the transaction
* happy path of transaction fulfillment 

### Whats worth to do
* GET RID OF JDBC, i tried it because it goes out of the box with VertX, but it's PAIN! 
This task can be completed with some JooQ, Hibernate etc.. 
* Handling of the transaction initiation error
* Retry if transaction capture has failed, it Async so can me processed until it will be successfull. 
* Handling of the transaction capturing errors
* Try to extend transaction locking, 
by adding some more advanced, 
probably some based on a proxy, in memory/redis Locking mechanism, 
that prevents locking money for the same account by more than ome worker at a time.
* Extend model by adding dates etc.
* Add packaging to the Docker image.
* CREATE MORE, MUCH MORE TESTS, ESPECIALLY FOR NEGATIVE CASES
* LOGGING
* Postman Project Can be more advanced
* Dockerize it via plugin or manually

### Requirements
__NOTE: This is DEMO, and it should not cover everything, I just tried to make it EXTENDABLE ENOUGH__

* System does not perform currency conversion for now
* System doGies not handle errors during background capturing money. It just send message to the queue, 
all future handlers can be implemented on demand.   

#####Cases covered
* Happy Path
* Handling of a few errors
* Balance Error
* Currency Error
* Internal Server error


#####Non Functional requirements:
* Simple
* Extendable
* Reliable

## How to Run
* cd to the directory with the project
* ``mvn clean package``
* ``java -jar target/moneytransfer-1.0.0-SNAPSHOT-fat.jar -conf src/conf/config.json``
* Import postman project ``moneytransfer.postman_collection.json`` and try.

## Flow Diagram
![Flow Diagram](https://github.com/ygalav/moneytransfer/blob/master/docs/flow-diagram.png?raw=true)
