package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class NonexistentBook extends AbstractEvent {

    private Long id;
    private Long bookId;
    private Long requestId;

    public NonexistentBook(Books aggregate) {
        super(aggregate);
    }

    public NonexistentBook() {
        super();
    }
}
//>>> DDD / Domain Event
