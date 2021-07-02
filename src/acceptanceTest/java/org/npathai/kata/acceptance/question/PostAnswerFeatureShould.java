package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Answer;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Answer posting feature should")
public class PostAnswerFeatureShould extends AcceptanceTestBase {
    private String answererId;
    private AnswerDsl answerDsl;
    private String questionId;

    @BeforeEach
    public void setUp() {
        UserDsl userDsl = new UserDsl(restTemplate);
        String originalPosterId = userDsl.registerUser().exec().getId();
        answererId = userDsl.registerUser().exec().getId();

        QuestionDsl questionDsl = new QuestionDsl(restTemplate);
        questionId = questionDsl.aQuestion().byUser(originalPosterId).exec().getId();

        answerDsl = new AnswerDsl(restTemplate);
    }

    @AcceptanceTest
    @DisplayName("return created answer in response")
    public void returnCreatedAnswer() {
        ResponseEntity<Answer> response = answerDsl.anAnswer()
                .onQuestion(questionId)
                .byUser(answererId)
                .withBody("An answer")
                .execReturningResponseEntity();

        assertThat(response.getBody()).isNotNull()
                .satisfies(answer -> {
                    assertThat(answer.getId()).isNotNull();
                    assertThat(answer.getAuthorId()).isEqualTo(answererId);
                    assertThat(answer.getQuestionId()).isEqualTo(questionId);
                    assertThat(answer.getBody()).isEqualTo("An answer");
                    assertThat(answer.getScore()).isEqualTo(0);
                });
    }
}
