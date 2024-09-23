package library.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import library.BooksApplication;
import library.domain.BookCheckedOUt;
import library.domain.BookRegistered;
import library.domain.NonexistentBook;
import lombok.Data;

@Entity
@Table(name = "Books_table")
@Data
//<<< DDD / Aggregate Root
public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long bookId;

    @PostPersist
    public void onPostPersist() {
        BookRegistered bookRegistered = new BookRegistered(this);
        bookRegistered.publishAfterCommit();

        BookCheckedOUt bookCheckedOUt = new BookCheckedOUt(this);
        bookCheckedOUt.publishAfterCommit();

        NonexistentBook nonexistentBook = new NonexistentBook(this);
        nonexistentBook.publishAfterCommit();
    }

    public static BooksRepository repository() {
        BooksRepository booksRepository = BooksApplication.applicationContext.getBean(
            BooksRepository.class
        );
        return booksRepository;
    }

    //<<< Clean Arch / Port Method
    public static void registerBook(BookDonated bookDonated) {
        //implement business logic here:

        /** Example 1:  new item 
        Books books = new Books();
        repository().save(books);

        BookRegistered bookRegistered = new BookRegistered(books);
        bookRegistered.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(bookDonated.get???()).ifPresent(books->{
            
            books // do something
            repository().save(books);

            BookRegistered bookRegistered = new BookRegistered(books);
            bookRegistered.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void checkOutBook(CheckoutRequested checkoutRequested) {
        //implement business logic here:

        /** Example 1:  new item 
        Books books = new Books();
        repository().save(books);

        BookCheckedOUt bookCheckedOUt = new BookCheckedOUt(books);
        bookCheckedOUt.publishAfterCommit();
        NonexistentBook nonexistentBook = new NonexistentBook(books);
        nonexistentBook.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(checkoutRequested.get???()).ifPresent(books->{
            
            books // do something
            repository().save(books);

            BookCheckedOUt bookCheckedOUt = new BookCheckedOUt(books);
            bookCheckedOUt.publishAfterCommit();
            NonexistentBook nonexistentBook = new NonexistentBook(books);
            nonexistentBook.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
