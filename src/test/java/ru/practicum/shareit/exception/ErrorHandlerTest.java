package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFound_returnsNotFoundWithMessage() {
        ResponseEntity<ErrorResponse> response =
                this.errorHandler.handleNotFound(new NotFoundException("Пользователь с id=1 не найден"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Пользователь с id=1 не найден");
    }

    @Test
    void handleForbidden_returnsForbiddenWithMessage() {
        ResponseEntity<ErrorResponse> response =
                this.errorHandler.handleForbidden(new ForbiddenException("Нет прав на редактирование"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Нет прав на редактирование");
    }

    @Test
    void handleConflict_returnsConflictWithMessage() {
        ResponseEntity<ErrorResponse> response =
                this.errorHandler.handleConflict(new ConflictException("Email уже используется"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Email уже используется");
    }

    @Test
    void handleValidation_returnsBadRequestWithMessage() {
        ResponseEntity<ErrorResponse> response =
                this.errorHandler.handleValidation(new ValidationException("Дата начала позже даты конца"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Дата начала позже даты конца");
    }

    @Test
    void handleThrowable_returnsInternalServerError() {
        ResponseEntity<ErrorResponse> response =
                this.errorHandler.handleThrowable(new IllegalStateException("Что-то пошло не так"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Что-то пошло не так");
    }
}
