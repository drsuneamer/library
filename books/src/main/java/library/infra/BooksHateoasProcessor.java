package library.infra;

import library.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class BooksHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Books>> {

    @Override
    public EntityModel<Books> process(EntityModel<Books> model) {
        return model;
    }
}
