package org.npathai.kata.application.api.question.answer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.api.validation.StringValidators;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostAnswerRequestPayloadValidatorShould {

    public static final String ANSWER_BODY = "Body";
    @Mock
    StringValidators stringValidators;

    @InjectMocks
    PostAnswerRequestPayloadValidator validator;
    private PostAnswerRequestPayload payload;

    @BeforeEach
    public void setUp() {
        payload = new PostAnswerRequestPayload();
        payload.setBody(ANSWER_BODY);
    }

    @Test
    public void validateAnswerBody() throws BadRequestParametersException {
        validator.validate(payload);

        verify(stringValidators).nonNullOrBlank(ANSWER_BODY);
    }

    @Test
    public void returnValidatedRequest() throws BadRequestParametersException {
        PostAnswerRequest request = validator.validate(payload);
        assertThat(request)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getBody()).isEqualTo(ANSWER_BODY);
                });
    }
}