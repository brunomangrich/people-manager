package sajadvpm.rest.controller;

import br.com.caelum.stella.validation.CPFValidator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sajadvpm.exception.NotFoundException;
import sajadvpm.feature.person.IPersonService;
import sajadvpm.feature.person.Person;
import sajadvpm.feature.person.command.PersonCommandRegister;
import sajadvpm.feature.person.command.PersonCommandUpdate;
import sajadvpm.rest.model.PersonDataGridModelView;
import sajadvpm.rest.model.PersonModelView;
import sajadvpm.util.MapperUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private IPersonService personService;

    public PersonController(IPersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Integer> post(@Valid @RequestBody PersonCommandRegister command) {

        logger.info("Iniciando o processo de internalização do registro na base.");

        Integer personId = Integer.MIN_VALUE;

        try {

            Person entity = MapperUtils.map(command, Person.class);

            personId = personService.add(entity, command.getAvatarId());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(personId, new HttpHeaders(), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Boolean> put(@Valid @RequestBody PersonCommandUpdate command) {

        logger.info("Iniciando o processo de atualização de registro com o id:" + command.getId()  + " na base ");

        Boolean isUpdate = Boolean.FALSE;

        try {

            Person entity = MapperUtils.map(command, Person.class);

            isUpdate = personService.update(entity, command.getAvatarId());

        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(isUpdate, new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/show")
    public ResponseEntity<List<PersonModelView>> show() {
        try {
            var entities = personService.getByActive();
            var models = MapperUtils.mapAll(entities, PersonDataGridModelView.class);
            return new ResponseEntity(models, new HttpHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonModelView> get(@PathVariable(required = true) Integer id) {

        try {

            Person entity = personService.get(id);

            var model = MapperUtils.map(entity, PersonModelView.class);

            if (entity.hasAvatar()) {
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/uploads/downloadFile/")
                        .path(entity.getAvatar().getPath())
                        .toUriString();

                model.setUrl(fileDownloadUri);
                model.setAvatarId(entity.getAvatar().getId());
            }

            return new ResponseEntity(model, new HttpHeaders(), HttpStatus.OK);
        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable(required = true) Integer id) {

        logger.info("Iniciando o processo de remoção do registro " + id  + "na base.");

        Boolean isDelete = Boolean.FALSE;

        try {
            isDelete = personService.delete(id);

        } catch (NotFoundException e) {
            logger.error(e.getMessage());
            return new ResponseEntity(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(isDelete, new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/checkCpfIsRepeated/{cpf}")
    public ResponseEntity<Boolean> checkCpfIsRepeated(@PathVariable(required = true) String cpf) {

        return  new ResponseEntity(personService.checkCpfIsRepeated(cpf), new HttpHeaders(), HttpStatus.OK);
    }
}
