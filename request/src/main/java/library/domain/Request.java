package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.RequestApplication;
import library.domain.CheckoutRequested;
import lombok.Data;

@Entity
@Table(name = "Request_table")
@Data
//<<< DDD / Aggregate Root
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long bookId;

    private Long requestId;

    private String status;

    @PostPersist
    public void onPostPersist() {
        CheckoutRequested checkoutRequested = new CheckoutRequested(this);
        checkoutRequested.publishAfterCommit();
    }

    public static RequestRepository repository() {
        RequestRepository requestRepository = RequestApplication.applicationContext.getBean(
            RequestRepository.class
        );
        return requestRepository;
    }

    public void requestCheckout() {
        //implement business logic here:

        CheckoutRequested checkoutRequested = new CheckoutRequested(this);
        checkoutRequested.publishAfterCommit();
    }

    //<<< Clean Arch / Port Method
    public static void updateStatus(AlreadyCheckedOut alreadyCheckedOut) {
        //implement business logic here:

        /** Example 1:  new item 
        Request request = new Request();
        repository().save(request);

        */

        /** Example 2:  finding and process
        
        repository().findById(alreadyCheckedOut.get???()).ifPresent(request->{
            
            request // do something
            repository().save(request);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
