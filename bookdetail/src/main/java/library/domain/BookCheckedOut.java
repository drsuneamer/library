package library.domain;

import java.time.LocalDate;
import java.util.*;
import library.infra.AbstractEvent;
import lombok.Data;

@Data
public class BookCheckedOut extends AbstractEvent {

    private Long id;
    private Long bookId;
    private String bookStatus;
}
