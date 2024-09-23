package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class AlreadyCheckedOut extends AbstractEvent {

    private Long id;
    private Long bookId;
    private Long checkoutId;

    public AlreadyCheckedOut(Books aggregate) {
        super(aggregate);
    }

    public AlreadyCheckedOut() {
        super();
    }
}
//>>> DDD / Domain Event
