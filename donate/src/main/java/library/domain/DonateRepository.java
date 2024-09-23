package library.domain;

import library.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "donates", path = "donates")
public interface DonateRepository
    extends PagingAndSortingRepository<Donate, Long> {}
