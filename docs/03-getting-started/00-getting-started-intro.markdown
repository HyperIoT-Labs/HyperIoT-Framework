# Getting Started [](id=getting-started)

You can find below all the necessary steps to let you download and run a project with HyperIoT Framework.

## Pre-Requisites

* Install Node and NPM on your machine
* Install yeoman generator with
    * ``` npm install -g yeoman ```
* Install HyperIoT generator with
    * ``` npm install -g generator-hyperiot -registry https://nexus.acsoftware.it/nexus/repository/npm-acs-public-repo ```
* Install Gradle up to 6.9 version
* Install JDK 11 or later

## Create a workspace

Let's call our example a Bookstore example. First of all let's create the workspace in which we will put our modules:

From the container folder let's type:

``` yo hyperiot:new-workspace ```

Now you have to answers few questions:

* Project Global Name: Bookstore
* HyperIoT Framework Version: 2.2.0
* Global Prefix: N
* Default module version: 1.0.0
* Custom repository : N

Let's go inside the workspace:

``` cd BookstoreWorkspace ```

Now, everything yo do with the generator you must do it inside this folder.
Even if you have to perform particular operation on a single module there's no need to move to the single module folder you can operate always from the workspace.

## create a project

Let's create our first project: Book which will contain all service related to Book entity.

``` yo hyperiot:new-project ```

Now you have to answers few questions:

* Project Name: Book
* Group Id: .....
* version: 1.0.0
* Application Type: Entity
* Actions should be registered ? yes
* Has rest services ? yes
* Deploy module to maven repo ? No

if you check the modules folder inside the workspace you will find the Book folder.

## customize your model

Let's open the workspace with you IDE.
Please choose gradle version 6.9, and import the gradle workspace (IntelliJ do it automatically after changing the gradle version to 6.9)

Let's go to the Book-model inside modules/Book/Book-model and open Book entity. Let's modify in order to add some simple fields:

![Framework invocation pipe](../images/book-model.png)

let's build everything with:

``` yo hyperiot:build-all ```

We are done!

## create a runtime

Let's create our custom Bookstore runtime based on HyperIoT Framework!
From the workspace let's run:

``` yo hyperiot:new-runtime ```

Now you have to answers few questions:

* Container name: bookstore
* group id: ...
* version: 1.0.0
* default web context root: bookstore
* database url: localhost
* database port: 5432
* database name: bookstore
* database username: bookstore
* database password: bookstore
* Install Cluster module (Zookeeper) ? No
* Install Kafka module ? No
* Select book module in order to be installed automatically

Now you should find the containers-src folder inside you workspace:

<workspace>/containers-src/bookstore

Inside the custom distribution folder you have:

* pom.xml already configured to generate custom karaf distribution with hyperiot framewrok and bookstore modules
* Dockerfile in order to create container version of you custom distribution
* docker-compose file in order to start postgres database with the right credentials chosen at the runtime generation time
* postman folder with a collection of rest method to invoke in order to interact with al the services.
* src folder to manage karaf configurations

Now we are ready to build our custom distribution and run it.
Inside the folder  <workspace>/containers-src/bookstore please run:

``` mvn clean package ```

It will create the target folder with inside the custom karaf distribution

## run it!

* Start docker engine

Inside the folder <workspace>/containers-src/bookstore

* run ``` docker-compose -f  ocker-compose-svil-basic.yml up ```
* run ./target/assebly/bin/karaf debug clean

Now open Postman and import all files inside <workspace>/containers-src/bookstore/postman both environment folder and the collection.

Run you services, before you invoke them you have to login with the authentication service!


## Example Project

You can finde the complete Bookstore example project [here](https://github.com/ACSoftwareTeam/BookstoreExample).