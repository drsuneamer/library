package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class BookDonated extends AbstractEvent {

    private Long id;
    private Long bookId;

    public BookDonated(Donate aggregate) {
        super(aggregate);
    }

    public BookDonated() {
        super();
    }
}
//>>> DDD / Domain Event
