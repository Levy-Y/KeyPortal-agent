.PHONY: build clean run setup

# TODO: Add option to build into native image

# Builds the application into a runnable jar, but does not make a fat jar
build:
	@./mvnw install

clean:
	@rm -rf ./target

run:
	@./mvnw quarkus:dev

# Used to setup the dev/prod environment
setup:
	@sudo docker compose up -d
