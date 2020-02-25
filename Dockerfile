FROM hirokimatsumoto/alpine-openjdk-11
COPY ./build/libs/candle-validation-1.0-SNAPSHOT.jar /app/candle-validation.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/candle-validation.jar"]