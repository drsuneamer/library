package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookCheckedOUt extends AbstractEvent {

    private Long id;
    private Long bookId;

    public BookCheckedOUt(Books aggregate) {
        super(aggregate);
    }

    public BookCheckedOUt() {
        super();
    }
}
//>>> DDD / Domain Event
