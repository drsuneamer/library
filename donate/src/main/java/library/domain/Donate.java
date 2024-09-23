package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.DonateApplication;
import library.domain.BookDonated;
import lombok.Data;

@Entity
@Table(name = "Donate_table")
@Data
//<<< DDD / Aggregate Root
public class Donate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long bookId;

    @PostPersist
    public void onPostPersist() {
        BookDonated bookDonated = new BookDonated(this);
        bookDonated.publishAfterCommit();
    }

    public static DonateRepository repository() {
        DonateRepository donateRepository = DonateApplication.applicationContext.getBean(
            DonateRepository.class
        );
        return donateRepository;
    }

    public void donateBook() {
        //implement business logic here:

        BookDonated bookDonated = new BookDonated(this);
        bookDonated.publishAfterCommit();
    }
}
//>>> DDD / Aggregate Root
