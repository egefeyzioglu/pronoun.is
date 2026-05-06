FROM docker.io/library/eclipse-temurin:21-jre-alpine

RUN apk add --no-cache words && ln -s /usr/share/dict/american-english /usr/share/dict/words

COPY target/pronouns-standalone.jar /pronoun.is/app.jar

COPY resources/ /resources

ENV PORT 3000

EXPOSE 3000

CMD ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/pronoun.is/app.jar" ]
