FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD my-site-1.0.2.RELEASE.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.datasource.druid.url=jdbc:mysql://172.17.0.1:3306/lu_tale?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false","-jar","/app.jar"]