package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Post question feature should")
public class PostQuestionFeatureShould extends AcceptanceTestBase {

    private QuestionDsl questionDsl;
    private String userId;

    @BeforeEach
    public void setUp() {
        UserDsl userDsl = new UserDsl(restTemplate);
        userId = userDsl.registerUser().exec().getId();
        questionDsl = new QuestionDsl(restTemplate);
    }

    @AcceptanceTest
    @DisplayName("return created answer in response")
    public void returnCreatedQuestion() {
        ResponseEntity<Question> response = questionDsl.aQuestion()
                .byUser(userId)
                .withTitle("Title")
                .withBody("Body")
                .withTags(List.of("java", "kata"))
                .execReturningResponseEntity();

        assertThat(response.getBody()).satisfies(question -> {
            assertThat(question.getId()).isNotBlank();
            assertThat(question.getAuthorId()).isEqualTo(userId);
            assertThat(question.getTitle()).isEqualTo("Title");
            assertThat(question.getBody()).isEqualTo("Body");
            assertThat(question.getTags()).map(Tag::getName)
                    .containsExactlyInAnyOrderElementsOf(List.of("java", "kata"));
            assertThat(question.getScore()).isEqualTo(0);
            assertThat(question.getAnswerCount()).isEqualTo(0);
        });
    }

    @AcceptanceTest
    @DisplayName("return 400 BAD_REQUEST status when question is invalid")
    public void return400BadRequestWhenQuestionIsInvalid() {
        ResponseEntity<Question> response = questionDsl.aQuestion()
                .byUser(userId)
                .withTitle("")
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
