package org.npathai.kata.application.api.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.validation.CollectionValidators;
import org.npathai.kata.application.domain.validation.StringValidators;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostQuestionRequestPayloadValidatorShould {

    @Mock
    StringValidators stringValidators;

    @Mock
    CollectionValidators collectionValidators;

    PostQuestionRequestPayload payload;

    @InjectMocks
    PostQuestionRequestPayloadValidator validator;

    @BeforeEach
    public void setUp() {
        payload = new PostQuestionRequestPayload();
        payload.setTitle("First question");
        payload.setBody("First question body");
        payload.setTags(List.of("java", "kata"));
    }

    @Test
    public void validateTitleIsNotNullOrBlank() throws BadRequestParametersException {
        validator.validate(payload);

        verify(stringValidators).nonNullOrBlank(payload.getTitle());
    }

    @Test
    public void validateBodyIsNotNullOrBlank() throws BadRequestParametersException {
        validator.validate(payload);

        verify(stringValidators).nonNullOrBlank(payload.getBody());
    }

    @Test
    public void validateTagsAreNotEmpty() throws BadRequestParametersException {
        validator.validate(payload);

        verify(collectionValidators).nonNullOrEmpty(payload.getTags());
    }

    @Test
    public void returnValidatedRequest() throws BadRequestParametersException {
        assertThat(validator.validate(payload))
                .isNotNull()
                .satisfies(request -> {
                    assertThat(request.getTitle()).isEqualTo(payload.getTitle());
                    assertThat(request.getTags()).isEqualTo(payload.getTags());
                    assertThat(request.getBody()).isEqualTo(payload.getBody());
                });
    }
}