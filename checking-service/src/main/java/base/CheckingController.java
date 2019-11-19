package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

@Controller
public class CheckingController {

    private static final String YES = "y";
    private static final String NO = "n";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckingController.class);

    private RabbitTemplate rabbitTemplate;
    private String dnaExchange;
    private String dnaRoutingKey;
    private String statsExchange;
    private String statsRoutingKey;

    private StatRepository repository;
    private long countMutantDna;
    private long countHumanDna;

    private long lastTime;
    private long maxElapsedTime;
    private Supplier<Optional> currentTimeSupplier;

    public CheckingController(final @Autowired RabbitTemplate rabbitTemplate,
                              final @Value("${dna.rabbitmq.exchange}") String dnaExchange,
                              final @Value("${dna.rabbitmq.routingkey}") String dnaRoutingKey,
                              final @Value("${stats.rabbitmq.exchange}") String statsExchange,
                              final @Value("${stats.rabbitmq.routingkey}") String statsRoutingKey,
                              final @Autowired StatRepository repository,
                              final Supplier<Optional> currentTimeSupplier,
                              final @Value("${stats.maxElapsedTime}") long maxElapsedTime) {

        this.rabbitTemplate = rabbitTemplate;

        this.dnaExchange = dnaExchange;
        this.dnaRoutingKey = dnaRoutingKey;

        this.statsExchange = statsExchange;
        this.statsRoutingKey = statsRoutingKey;

        this.repository = repository;
        this.countMutantDna = 0;
        this.countHumanDna = 0;

        this.currentTimeSupplier = currentTimeSupplier;
        this.maxElapsedTime = maxElapsedTime;
        this.lastTime = 0;
    }

    @PostMapping("/mutant/")
    public ResponseEntity mutantCheck(final @RequestBody DNASample sample) throws JsonProcessingException {

        String sampleStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(sample);

        HttpStatus status;

        if (MutantTester.isMutant(sample.getDna())) {

            saveDnaSample(sampleStr);
            saveStat(YES);
            status = HttpStatus.OK;

        } else {
            saveStat(NO);
            status = HttpStatus.FORBIDDEN;
        }

        LOGGER.debug("response : " + status + " --- dna sample : " + sampleStr);

        return ResponseEntity.status(status).build();
    }

    @GetMapping("/stats/")
    public ResponseEntity stats() {

        if ((long) currentTimeSupplier.get().get() - lastTime > maxElapsedTime) {
            updateStats();
            lastTime = (long) currentTimeSupplier.get().get();
        }

        StatResponse statResponse = new StatResponse();
        statResponse.setCountMutantDna(countMutantDna);
        statResponse.setCountHumanDna(countHumanDna);

        if (countHumanDna != 0) {
            statResponse.setRatio( ((float) countMutantDna) / ((float) countHumanDna) );
        } else {
            statResponse.setRatio(countMutantDna);
        }

        return ResponseEntity.status(HttpStatus.OK).body(statResponse);
    }

    private void saveDnaSample(final String sampleStr) {
        try {
            rabbitTemplate.convertAndSend(dnaExchange, dnaRoutingKey, sampleStr);
        } catch (Exception e) {
            LOGGER.debug("Error when trying to save dna sample", e);
        }
    }

    private void saveStat(final String stat) {
        try {
            rabbitTemplate.convertAndSend(statsExchange, statsRoutingKey, stat);
        } catch (Exception e) {
            LOGGER.debug("Error when trying to save stat sample", e);
        }
    }

    private void updateStats() {

        Iterator<Stat> statIterator = this.repository.findAll().iterator();

        if (statIterator.hasNext()) {

            Stat stat = statIterator.next();

            countMutantDna = stat.getCountMutantDna();
            countHumanDna = stat.getCountHumanDna();
        }

        LOGGER.debug("countMutantDna : " + countMutantDna + " -- countHumanDna : " + countHumanDna);
    }
}
