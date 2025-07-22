.PHONY: build clean run

# Builds the application into a runnable jar, but does not make a fat jar
build:
	@./mvnw install

clean:
	@rm -rf ./target

run:
	@./mvnw quarkus:dev