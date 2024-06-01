
single-instance:
	docker compose \
	-f docker/compose-individual-with-individual-cache-individual-db.yaml \
	up \
	--build

replicas-individual-cache-individual-db:
	docker compose \
	-f docker/compose-replicas-with-individual-cache-individual-db.yaml \
	up \
	--build

replicas-individual-cache-shared-db:
	docker compose \
	-f docker/compose-replicas-with-individual-cache-shared-db.yaml \
	up \
	--build

replicas-shared-cache-shared-db:
	docker compose \
	-f docker/compose-replicas-with-shared-cache-shared-db.yaml \
	up \
	--build

down:
	-docker compose \
	-f docker/compose-individual-with-individual-cache-individual-db.yaml \
	down

	-docker compose \
	-f docker/compose-replicas-with-individual-cache-individual-db.yaml \
	down


	-docker compose \
	-f docker/compose-replicas-with-individual-cache-shared-db.yaml \
	down

	-docker compose \
	-f docker/compose-replicas-with-shared-cache-shared-db.yaml \
	down

clean: down
	-docker rmi "consartist/blog-spring-cacheable"