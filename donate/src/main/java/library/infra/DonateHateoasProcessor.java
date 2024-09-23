package library.infra;

import library.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class DonateHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Donate>> {

    @Override
    public EntityModel<Donate> process(EntityModel<Donate> model) {
        return model;
    }
}
