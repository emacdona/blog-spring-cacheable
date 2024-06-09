
# https://tech.davis-hansson.com/p/make/
SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

apphost = host
replica-count = 5
request-count = 99
isbn = 0130305529
original-title = "On%20Lisp"
new-title = "HELLO%20WORLD"

# --------------------------------------------------------------------------------
# Targets that start the application
# --------------------------------------------------------------------------------
.PHONY: single-instance
single-instance:
	docker compose \
	-f docker/compose-individual-with-individual-cache-individual-db.yaml \
	up \
	--build

.PHONY: replicas-individual-cache-individual-db
replicas-individual-cache-individual-db:
	REPLICA_COUNT=$(replica-count) \
	docker compose \
	-f docker/compose-replicas-with-individual-cache-individual-db.yaml \
	up \
	--build

.PHONY: replicas-individual-cache-shared-db
replicas-individual-cache-shared-db:
	REPLICA_COUNT=$(replica-count) \
	docker compose \
	-f docker/compose-replicas-with-individual-cache-shared-db.yaml \
	up \
	--build

.PHONY: replicas-shared-cache-shared-db
replicas-shared-cache-shared-db:
	REPLICA_COUNT=$(replica-count) \
	docker compose \
	-f docker/compose-replicas-with-shared-cache-shared-db.yaml \
	up \
	--build

# --------------------------------------------------------------------------------
# Targets that clean up
# --------------------------------------------------------------------------------
.PHONY: down
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

.PHONY: clean
clean: down
	-docker rmi "consartist/blog-spring-cacheable"

# --------------------------------------------------------------------------------
# Targets that exercise the application
# --------------------------------------------------------------------------------
.PHONY: clear-cache-for-all-replicas
clear-cache-for-all-replicas:
	for i in {1..$(replica-count)};
	do
		curl -s http://$(apphost):8080/books/clear;
	done

.PHONY: restore-title-for-all-replicas
restore-title-for-all-replicas:
	for i in {1..$(replica-count)};
	do
		curl -s http://$(apphost):8080/books/$(isbn)/bestUpdateTitle/$(original-title) | jq -c '.'
	done

.PHONY: get-book
get-book:
	for i in {1..99};
	do
		curl -s http://$(apphost):8080/books/$(isbn) | jq -c '. | {host, cached,title}';
	done | sort | uniq -c

.PHONY: bad-update-title
bad-update-title:
	curl -s http://$(apphost):8080/books/$(isbn)/badUpdateTitle/$(new-title)%20BAD | jq -c '.'

.PHONY: better-update-title
better-update-title:
	curl -s http://$(apphost):8080/books/$(isbn)/betterUpdateTitle/$(new-title)%20BETTER | jq -c '.'

.PHONY: best-update-title
best-update-title:
	curl -s http://$(apphost):8080/books/$(isbn)/bestUpdateTitle/$(new-title)%20BEST | jq -c '.'

.PHONY: clear-caches-and-get-book
clear-caches-and-get-book: clear-cache-for-all-replicas get-book
