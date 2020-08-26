##Pre-requisites
1. JAVA 11

##Build
```shell script
  ./gradlew clean build
```

##Run
```
    java -jar buid/libs/container-status-0.0.1.jar
```

##Directory paths
####Input
1. path = ${HOME}/container-status/input-files/
2. format = any text based file with container id on each line

####Output
1. path = ${HOME}/container-status/output-files/
2. filenames :<br>
    I.  **Inventory.csv** -> containers status with inventory<br>
    II. **Position.csv** -> containers in transit in train.<br>
