package library.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import library.config.kafka.KafkaProcessor;
import library.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    RequestRepository requestRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='NonexistentBook'"
    )
    public void wheneverNonexistentBook_UpdateStatus(
        @Payload NonexistentBook nonexistentBook
    ) {
        NonexistentBook event = nonexistentBook;
        System.out.println(
            "\n\n##### listener UpdateStatus : " + nonexistentBook + "\n\n"
        );

        // Sample Logic //
        Request.updateStatus(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
