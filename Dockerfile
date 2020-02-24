FROM hirokimatsumoto/alpine-openjdk-11
COPY ./build/libs/S3-backup-verification-1.0-SNAPSHOT.jar /app/S3-backup-verification.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/S3-backup-verification.jar"]