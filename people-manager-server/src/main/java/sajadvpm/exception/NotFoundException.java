package sajadvpm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends BusinessException {

    public NotFoundException(Integer id) {
        super("Não foi possível encontrar o registro:" + id);
    }
}