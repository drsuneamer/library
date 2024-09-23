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
    BooksRepository booksRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='BookDonated'"
    )
    public void wheneverBookDonated_RegisterBook(
        @Payload BookDonated bookDonated
    ) {
        BookDonated event = bookDonated;
        System.out.println(
            "\n\n##### listener RegisterBook : " + bookDonated + "\n\n"
        );

        // Sample Logic //
        Books.registerBook(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='CheckoutRequested'"
    )
    public void wheneverCheckoutRequested_CheckOutBook(
        @Payload CheckoutRequested checkoutRequested
    ) {
        CheckoutRequested event = checkoutRequested;
        System.out.println(
            "\n\n##### listener CheckOutBook : " + checkoutRequested + "\n\n"
        );

        // Sample Logic //
        Books.checkOutBook(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
