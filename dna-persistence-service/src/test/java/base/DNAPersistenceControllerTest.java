package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class DNAPersistenceControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private DNASampleRepository repository;

    private DNAPersistenceController controller;

    @BeforeEach
    public void init() {
        controller = new DNAPersistenceController(repository);
    }

    @Test
    public void persistenceTest() throws IOException {

        DNASample sample = new DNASample();
        sample.setDna(Arrays.asList("aaaa", "tttt", "cccc", "gggg"));

        String sampleStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(sample);

        controller.listener(sampleStr);

        Iterable<DNASample> samplesIt = repository.findAll();
        List<DNASample> samples = Lists.newArrayList(samplesIt);

        Assertions.assertEquals(1, samples.size());
        Assertions.assertEquals(sample.getDna().size(), samples.get(0).getDna().size());
    }

}
