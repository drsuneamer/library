package library.infra;

import java.util.List;
import library.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "bookdetails",
    path = "bookdetails"
)
public interface BookdetailRepository
    extends PagingAndSortingRepository<Bookdetail, Long> {
    List<Bookdetail> findByBookId(Long bookId);
}
