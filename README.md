# **LibAccount**
[![Build Status](https://travis-ci.com/ninja-modding/LibAccount.svg?branch=master)](https://travis-ci.com/ninja-modding/LibAccount) [![BCH compliance](https://bettercodehub.com/edge/badge/ninja-modding/LibAccount?branch=master)](https://bettercodehub.com/)

LibAccount is an api that allows users to create accounts and authenticate. This library connects to a MySQL database.

## Prerequisites
- MySQL Server

### MySQL installation (Ubuntu)

1. Install MySQL
```
$ sudo apt update
$ sudo apt install mysql-server maven git
```

2. Configure MySQL root user
```
$ sudo mysql_secure_installation
```

3. Create LibAccount user and database
```
$ sudo mysql
mysql> CREATE USER 'username'@'localhost' IDENTIFIED BY 'password';
mysql> GRANT ALL PRIVILEGES ON *.* TO 'username'@'localhost' WITH GRANT OPTION;
mysql> CREATE DATABASE LibAccount;
mysql> exit
```
Replace username with the name of the user you want to create. (ie. libaccount) And replace the password with a new password. We will need the username and password later when we setup LibAccount.

## Installation
In order to use this in a project, simply add the maven repository and dependency as shown here.

##### Repository
```
  <repository>
            <id>oss.sonatype.org-snapshot</id>
            <url>http://oss.sonatype.org/content/repositories/snapshots</url>
  </repository>
```

##### Dependency
  ``` 
   <dependency>
             <groupId>com.ninjamodding</groupId>
             <artifactId>LibAccount</artifactId>
             <version>1.2.6-SNAPSHOT</version>
             <scope>compile</scope>
   </dependency>
   ```
   ## Tests
   To run the test, you must set some environment variables in order to connect to the database.
   
   ```
   $ export user=(Your MySQL username)
   $ export password=(Your MySQL password)
   $ export database=LibAccount
   $ export ip=localhost
   ```
   
   Next clone the repository.
   ```
   $ git clone https://github.com/ninja-modding/LibAccount.git
   $ cd LibAccount
   ```
   
   Finally run the tests with maven.
   ```
   mvn test
   ```
