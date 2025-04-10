.PHONY: kafka format-kraft clean-kraft

KAFKA_DIR=./kafka
KAFKA_BIN=$(KAFKA_DIR)/bin
CONFIG=$(KAFKA_DIR)/config/server.properties
LOG_DIR=/tmp/kraft-combined-logs

kafka: format-kraft
	$(KAFKA_BIN)/kafka-server-start.sh $(CONFIG)

format-kraft:
	@echo "Initializing KRaft storage..."
	@mkdir -p $(LOG_DIR)
	@chmod 755 $(LOG_DIR)
	$(eval CLUSTER_ID := $(shell $(KAFKA_BIN)/kafka-storage.sh random-uuid))
	$(KAFKA_BIN)/kafka-storage.sh format --cluster-id $(CLUSTER_ID) --config $(CONFIG)

clean-kraft:
	rm -rf $(LOG_DIR)/*

make compose-start:
	cd docker && docker-compose up --build
make compose-down:
	cd docker && docker-compose down -v
make docker-kafka:
	docker pull apache/kafka:4.0.0 && docker run -p 9092:9092 apache/kafka:4.0.0