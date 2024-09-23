package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class CheckoutRequested extends AbstractEvent {

    private Long id;
    private Long bookId;
    private Long requestId;
    private String status;

    public CheckoutRequested(Request aggregate) {
        super(aggregate);
    }

    public CheckoutRequested() {
        super();
    }
}
//>>> DDD / Domain Event
