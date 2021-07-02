package org.npathai.kata.application.api.vote;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VoteControllerShould {

    @Mock
    private QuestionService questionService;

    @Mock
    private VoteRequestPayloadValidator validator;

    @InjectMocks
    private VoteController voteController;

    private static final String USER_ID = "U1";
    private static final String QUESTION_ID = "Q1";
    private VoteRequestPayload payload;
    private Score score;

    @BeforeEach
    public void setUp() {
        payload = new VoteRequestPayload();
        payload.setType(VoteType.UP.val);

        score = new Score();
        score.setScore(1);
    }

    @Test
    @SneakyThrows
    public void returnTheScore() {
        VoteRequest request = VoteRequest.valid(VoteType.UP);
        given(validator.validate(payload)).willReturn(request);
        given(questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), request))
                .willReturn(score);

        ResponseEntity<Score> responseEntity = voteController.voteQuestion(USER_ID, QUESTION_ID, payload);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isSameAs(score);
    }

    @Test
    @SneakyThrows
    public void return400BadRequestWhenPayloadIsInvalid() {
        given(validator.validate(payload)).willThrow(BadRequestParametersException.class);

        ResponseEntity<Score> response = voteController.voteQuestion(USER_ID, QUESTION_ID, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SneakyThrows
    public void return400BadRequestWhenOperationIsNotPermitted() {
        VoteRequest request = VoteRequest.valid(VoteType.UP);
        given(validator.validate(payload)).willReturn(request);
        given(questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), request))
                .willThrow(ImpermissibleOperationException.class);
        
        ResponseEntity<Score> response = voteController.voteQuestion(USER_ID, QUESTION_ID, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
