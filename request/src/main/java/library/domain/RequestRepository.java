package library.domain;

import library.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "requests", path = "requests")
public interface RequestRepository
    extends PagingAndSortingRepository<Request, Long> {}
