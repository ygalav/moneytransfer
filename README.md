# moneytransfer

### Purpose
The purpose of this task is to learn Vertx + RxJava usage. 
The task is develop the system, for transferring money between accounts. 

### Requirements
__NOTE: This is DEMO, and it should not cover everything, I just tried to make it EXTENDABLE ENOUGH__

* System does not perform currency conversion for now
* System does not handle errors during background capturing money. It just send message to the queue, 
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



## Flow Diagram
![Flow Diagram](https://github.com/ygalav/moneytransfer/blob/master/docs/flow-diagram.png?raw=true)
