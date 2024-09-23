package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookCheckedOut extends AbstractEvent {

    private Long id;
    private Long bookId;
    private String bookStatus;

    public BookCheckedOut(Books aggregate) {
        super(aggregate);
    }

    public BookCheckedOut() {
        super();
    }
}
//>>> DDD / Domain Event
