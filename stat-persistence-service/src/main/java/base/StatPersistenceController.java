package base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class StatPersistenceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatPersistenceController.class);

    private static final String YES = "y";
    private static final String NO = "n";

    private StatRepository repository;
    private Stat localStat = new Stat();
    private long countMutantDna;
    private long countHumanDna;

    public StatPersistenceController(final @Autowired StatRepository repository,
                                     final @Autowired ScheduledExecutorService scheduler,
                                     final @Value("${schedulerUpdateTime}") long schedulerUpdateTime) {

        this.repository = repository;

        Iterator<Stat> statIterator = this.repository.findAll().iterator();

        if (statIterator.hasNext()) {

            Stat stat = statIterator.next();

            localStat.setId(stat.getId());
            countMutantDna = stat.getCountMutantDna();
            countHumanDna = stat.getCountHumanDna();

        } else {
            countMutantDna = 0;
            countHumanDna = 0;
        }

        scheduler.scheduleWithFixedDelay(() -> updateStats(), 100L, schedulerUpdateTime, TimeUnit.MILLISECONDS);
    }

    @RabbitListener(queues="${rabbitmq.queue}")
    public void listener (String strSample) {

        if (strSample != null) {

            if (strSample.equalsIgnoreCase(YES)) countMutantDna++;
            if (strSample.equalsIgnoreCase(NO)) countHumanDna++;

            LOGGER.debug("stat: " + strSample);
        }
    }

    private void updateStats() {
        localStat.setCountMutantDna(countMutantDna);
        localStat.setCountHumanDna(countHumanDna);
        saveStatSample(localStat);
    }

    private void saveStatSample(final Stat sample) {
        try {
            repository.save(sample);
        } catch (Exception e) {
            LOGGER.debug("Error when trying to save stat sample", e);
        }
    }
}
