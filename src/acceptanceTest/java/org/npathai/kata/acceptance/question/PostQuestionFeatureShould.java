package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PostQuestionFeatureShould extends AcceptanceTestBase {

    private UserDsl userDsl;
    private QuestionDsl questionDsl;
    private String userId;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        userId = userDsl.registerUser()
                .withUsername("username")
                .withEmail("user@mail.com")
                .exec()
                .getId();
    }

    @Test
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
            assertThat(question.getAnswerCount()).isEqualTo(0);
        });
    }
}
