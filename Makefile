
single-instance:
	docker compose -f docker/compose-individual-with-individual-cache.yaml up --build

replicas-multi-cache:
	docker compose -f docker/compose-replicas-with-individual-cache.yaml up --build

replicas-single-cache:
	docker compose -f docker/compose-replicas-with-shared-cache.yaml up --build

down:
	-docker compose -f docker/compose-individual-with-individual-cache.yaml down
	-docker compose -f docker/compose-replicas-with-individual-cache.yaml down
	-docker compose -f docker/compose-replicas-with-shared-cache.yaml down

clean: down
	-docker rmi "consartist/blog-spring-cacheable"