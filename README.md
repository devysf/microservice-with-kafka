Testing locally can be done as follows:

1. Run the following command in terminal 
```
docker-compose up -d
```
This will enable us to run the Postgres, Zookeeper, Kafka and Kafka Manager containers on the ports we specify in the docker-compose.yaml file.

2. Run the following command to compile the maven project
```
mvn clean install -DskipTests
```

3. Run ProducerMicroservicesApplication and ConsumerMicroservicesApplication 

4. Make requests to these endpoints
   
http://localhost:8081/producerDomain/save

http://localhost:8081/producerDomain/saveInvalidData
