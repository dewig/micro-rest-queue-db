package base;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class StatPersistenceControllerTest {

    private static final String YES = "y";
    private static final String NO = "n";

    @Autowired
    private StatRepository repository;

    @Mock
    private ScheduledExecutorService scheduler;
    private ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    private Runnable updateStats;

    private StatPersistenceController controller;

    @BeforeEach
    public void init() {

        doReturn(null).when(scheduler).scheduleWithFixedDelay(runnableCaptor.capture(),
                any(long.class), any(long.class), any(TimeUnit.class));

        controller = new StatPersistenceController(repository, scheduler, 1000L);

        updateStats = runnableCaptor.getValue();
    }

    @Test
    public void persistenceTestWithEmptyDB() {

        List<Stat> samples;

        controller.listener(YES);
        controller.listener(NO);
        updateStats.run();

        samples = Lists.newArrayList(repository.findAll());

        Assertions.assertEquals(1, samples.size());
        Assertions.assertEquals(1, samples.get(0).getCountMutantDna());
        Assertions.assertEquals(1, samples.get(0).getCountHumanDna());

        controller.listener(YES);
        controller.listener(NO);
        updateStats.run();

        samples = Lists.newArrayList(repository.findAll());

        Assertions.assertEquals(1, samples.size());
        Assertions.assertEquals(2, samples.get(0).getCountMutantDna());
        Assertions.assertEquals(2, samples.get(0).getCountHumanDna());
    }

    @Test
    public void persistenceTestWithNonEmptyDB() {

        Stat stat = new Stat();
        stat.setCountMutantDna(10);
        stat.setCountHumanDna(20);
        repository.save(stat);

        StatPersistenceController controller =
                new StatPersistenceController(repository, scheduler, 1000L);

        List<Stat> samples;

        controller.listener(YES);
        controller.listener(NO);
        runnableCaptor.getValue().run();

        samples = Lists.newArrayList(repository.findAll());

        Assertions.assertEquals(1, samples.size());
        Assertions.assertEquals(11, samples.get(0).getCountMutantDna());
        Assertions.assertEquals(21, samples.get(0).getCountHumanDna());
    }

}
