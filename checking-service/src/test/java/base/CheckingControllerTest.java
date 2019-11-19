package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class CheckingControllerTest {

    @Autowired
    private StatRepository repository;
    private Supplier<Optional> currentTimeSupplier;
    private long maxElapsedTime = 0;

    private CheckingController controller;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void init() {

        currentTimeSupplier = () -> Optional.of(10L);

        controller = new CheckingController(rabbitTemplate,
                "dna","", "stats", "",
                repository, currentTimeSupplier, maxElapsedTime);
    }

    @Test
    public void responseOK() throws JsonProcessingException {

        DNASample sample = new DNASample();
        sample.setDna(Arrays.asList("aaaa", "tttt", "cccc", "gggg"));

        ResponseEntity result = controller.mutantCheck(sample);

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("dna"), any(String.class), any(String.class));

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("stats"), any(String.class), eq("y"));

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void responseFORBIDDEN() throws JsonProcessingException {

        DNASample sample = new DNASample();
        sample.setDna(Arrays.asList("atat", "cgcg", "atat", "cgcg"));

        ResponseEntity result = controller.mutantCheck(sample);

        verify(rabbitTemplate, never())
                .convertAndSend(eq("dna"), any(String.class), any(String.class));

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("stats"), any(String.class), eq("n"));

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void persistenceTestWithEmptyDB() {

        StatResponse stats = (StatResponse) controller.stats().getBody();

        Assertions.assertEquals(0, stats.getCountMutantDna());
        Assertions.assertEquals(0, stats.getCountHumanDna());
        Assertions.assertEquals(0, stats.getRatio());
    }

    @Test
    public void persistenceTestWithNonEmptyDB() {

        Stat stat = new Stat();

        stat.setId(1);
        stat.setCountMutantDna(10);
        stat.setCountHumanDna(40);
        repository.save(stat);

        StatResponse stats;

        stats = (StatResponse) controller.stats().getBody();

        Assertions.assertEquals(10, stats.getCountMutantDna());
        Assertions.assertEquals(40, stats.getCountHumanDna());
        Assertions.assertEquals(0.25, stats.getRatio());

        stats = (StatResponse) controller.stats().getBody();

        Assertions.assertEquals(10, stats.getCountMutantDna());
        Assertions.assertEquals(40, stats.getCountHumanDna());
        Assertions.assertEquals(0.25, stats.getRatio());
    }

}
