package library.domain;

import java.util.*;
import library.domain.*;
import library.infra.AbstractEvent;
import lombok.*;

@Data
@ToString
public class AlreadyCheckedOut extends AbstractEvent {

    private Long id;
    private Long bookId;
    private Long checkoutId;
}
