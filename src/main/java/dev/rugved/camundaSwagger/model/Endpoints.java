package dev.rugved.camundaSwagger.model;

import lombok.Data;

@Data
public class Endpoints {
    private KafkaConfig kafka;

    public KafkaConfig getKafka() {
        return kafka;
    }

    public void setKafka(KafkaConfig kafka) {
        this.kafka = kafka;
    }
}
