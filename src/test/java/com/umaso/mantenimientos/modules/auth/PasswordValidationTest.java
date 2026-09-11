package com.umaso.mantenimientos.modules.auth;

import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.users.dto.request.CreateUserRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidationTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        FACTORY.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "12345678", "abcdefgh", "ABCDEFGH", "!!!!!!!!"})
    void acceptsTemporaryPasswordsWithoutCompositionRequirements(String password) {
        assertThat(VALIDATOR.validate(new CreateUserRequest(
                "Ismael", "ismael@example.com", password, UUID.randomUUID()))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "        "})
    void rejectsShortOrBlankTemporaryPasswords(String password) {
        assertThat(VALIDATOR.validate(new CreateUserRequest(
                "Ismael", "ismael@example.com", password, UUID.randomUUID()))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ismael2026", "abcdefg1", "ABCDEFG1", "12345678"})
    void acceptsNewPasswordsWithEightCharactersAndANumber(String password) {
        assertThat(VALIDATOR.validate(new ChangePasswordRequest("123456789", password))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc1234", "abcdefgh", "ABCDEFGH", "Ismael!!", "        "})
    void rejectsNewPasswordsWithoutMinimumLengthOrNumber(String password) {
        assertThat(VALIDATOR.validate(new ChangePasswordRequest("123456789", password))).isNotEmpty();
    }
}
