package com.kholodilin.outbox.api;

import com.kholodilin.idempotency.exception.IdempotencyConflictException;
import com.kholodilin.idempotency.model.IdempotencyKey;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIdempotencyConflictTo409ProblemDetail() {
        IdempotencyConflictException ex = new IdempotencyConflictException(
                new IdempotencyKey("CREATE_ORDER:1", "key-1"),
                "hash-a",
                "hash-b"
        );
        ProblemDetail problem = handler.handleIdempotencyConflict(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Idempotency conflict");
        assertThat(problem.getDetail()).isEqualTo(ex.getMessage());
        assertThat(problem.getType()).isNotNull();
    }
}
