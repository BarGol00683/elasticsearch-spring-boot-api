# elasticsearch-spring-boot-api

* This project was created to showcase how to configure external elasticsearch API with spring-boot and gradle. 
org.springframework.data contains spring-data-elasticsearch which conflicts with ES, also it's not compatible with newest elasticsearch. The reason is that spring-data-elasticsearch works on old RestClient which is deprecated while working on one of the newest ES API.
