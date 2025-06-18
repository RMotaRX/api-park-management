# create the container with bitnami/postgresql and localstack using path ./docker-compose.yml
pg_up:
	docker-compose up --build -d

# build the full application running all test and checks
check:
	mvn clean install

# build the application without tests
build:
	mvn clean install -DskipTests