package library.infra;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import library.config.kafka.KafkaProcessor;
import library.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class BookdetailViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private BookdetailRepository bookdetailRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookRegistered_then_CREATE_1(
        @Payload BookRegistered bookRegistered
    ) {
        try {
            if (!bookRegistered.validate()) return;

            // view 객체 생성
            Bookdetail bookdetail = new Bookdetail();
            // view 객체에 이벤트의 Value 를 set 함
            bookdetail.setBookId(bookRegistered.getBookId());
            bookdetail.setStatus("등록됨");
            // view 레파지 토리에 save
            bookdetailRepository.save(bookdetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookCheckedOUt_then_UPDATE_1(
        @Payload BookCheckedOUt bookCheckedOUt
    ) {
        try {
            if (!bookCheckedOUt.validate()) return;
            // view 객체 조회

            List<Bookdetail> bookdetailList = bookdetailRepository.findByBookId(
                bookCheckedOUt.getBookId()
            );
            for (Bookdetail bookdetail : bookdetailList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                bookdetail.setStatus("대여완료됨");
                // view 레파지 토리에 save
                bookdetailRepository.save(bookdetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
