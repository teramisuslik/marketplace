package com.example.serviceforpay.integration;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.example.serviceforpay.DTO.BuyProductDTO;
import com.example.serviceforpay.service.PayService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"buy-product"})
class KafkaListenerTest {

    @Autowired
    private KafkaTemplate<String, BuyProductDTO> kafkaTemplate;

    @MockitoSpyBean
    private PayService payService;

    @TestConfiguration
    static class KafkaTestConfig {

        @Bean
        public ProducerFactory<String, BuyProductDTO> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
            return new DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        public KafkaTemplate<String, BuyProductDTO> kafkaTemplate(
                ProducerFactory<String, BuyProductDTO> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
    }

    @Test
    void shouldConsumeMessageAndCallPayService() throws Exception {
        // Given
        BuyProductDTO dto = BuyProductDTO.builder().productId(999L).userId(888L).build();

        // When
        kafkaTemplate.send("buy-product", dto).get(5, TimeUnit.SECONDS);

        // Then
        verify(payService, timeout(5000).times(1)).printMassage(dto);
    }
}
