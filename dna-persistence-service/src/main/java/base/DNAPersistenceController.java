package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DNAPersistenceController {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(DNAPersistenceController.class);

    private DNASampleRepository repository;

    public DNAPersistenceController(@Autowired DNASampleRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues="${rabbitmq.queue}")
    public void listener (String strSample) throws IOException {

        DNASample sample = MAPPER.readValue(strSample, DNASample.class);
        saveDnaSample(sample);
        LOGGER.debug("dna sample: " + strSample);
    }

    private void saveDnaSample(final DNASample sample) {
        try {
            repository.save(sample);
        } catch (Exception e) {
            LOGGER.debug("Error when trying to save dna sample", e);
        }
    }
}
