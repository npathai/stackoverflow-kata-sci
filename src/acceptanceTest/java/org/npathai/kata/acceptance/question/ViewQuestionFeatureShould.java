package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.QuestionWithAnswers;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("View question feature should")
public class ViewQuestionFeatureShould extends AcceptanceTestBase {

    private QuestionDsl questionDsl;
    private AnswerDsl answerDsl;
    private String opId;
    private String questionId;
    private String answerer1Id;
    private String answerer2Id;

    @BeforeEach
    public void setUp() {
        UserDsl userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        answerDsl = new AnswerDsl(restTemplate);

        opId = userDsl.registerUser()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec().getId();

        answerer1Id = userDsl.registerUser()
                .withUsername("harry")
                .withEmail("harry@gmail.com")
                .exec().getId();

        answerer2Id = userDsl.registerUser()
                .withUsername("alice")
                .withEmail("alice@gmail.com")
                .exec().getId();

        questionId = questionDsl.aQuestion()
                .byUser(opId)
                .withTitle("Question")
                .withBody("Question body")
                .withTags(List.of("kata", "java"))
                .exec().getId();
    }

    @AcceptanceTest
    @DisplayName("return question without answers when unanswered")
    public void returnQuestionWithoutAnswersWhenUnanswered() {
        QuestionWithAnswers questionWithAnswers = questionDsl.getQuestionById(questionId).exec();

        assertThat(questionWithAnswers.getQuestion()).satisfies(q -> {
            assertThat(q.getId()).isNotBlank();
            assertThat(q.getAuthorId()).isEqualTo(opId);
            assertThat(q.getTitle()).isEqualTo("Question");
            assertThat(q.getBody()).isEqualTo("Question body");
            assertThat(q.getTags()).map(Tag::getName)
                    .containsExactlyInAnyOrderElementsOf(List.of("java", "kata"));
        });
        assertThat(questionWithAnswers.getAnswers()).isEmpty();
    }

    @AcceptanceTest
    @DisplayName("return question with all answers")
    public void returnQuestionWithAllAnswers() {
        String answer1Id = answerDsl.anAnswer()
                .byUser(answerer1Id)
                .onQuestion(questionId)
                .withBody("This is answer 1")
                .exec().getId();

        String answer2Id = answerDsl.anAnswer()
                .byUser(answerer2Id)
                .onQuestion(questionId)
                .withBody("This is answer 2")
                .exec().getId();

        QuestionWithAnswers questionWithAnswers = questionDsl.getQuestionById(questionId).exec();

        assertThat(questionWithAnswers.getQuestion()).satisfies(q -> {
            assertThat(q.getId()).isNotBlank();
            assertThat(q.getAuthorId()).isEqualTo(opId);
            assertThat(q.getTitle()).isEqualTo("Question");
            assertThat(q.getBody()).isEqualTo("Question body");
            assertThat(q.getTags()).map(Tag::getName)
                    .containsExactlyInAnyOrderElementsOf(List.of("java", "kata"));
        });

        assertThat(questionWithAnswers.getAnswers()).anySatisfy(a -> {
            assertThat(a.getId()).isEqualTo(answer1Id);
            assertThat(a.getQuestionId()).isEqualTo(questionId);
            assertThat(a.getAuthorId()).isEqualTo(answerer1Id);
            assertThat(a.getBody()).isEqualTo("This is answer 1");
        });

        assertThat(questionWithAnswers.getAnswers()).anySatisfy(a -> {
            assertThat(a.getId()).isEqualTo(answer2Id);
            assertThat(a.getQuestionId()).isEqualTo(questionId);
            assertThat(a.getAuthorId()).isEqualTo(answerer2Id);
            assertThat(a.getBody()).isEqualTo("This is answer 2");
        });
    }

    @AcceptanceTest
    @DisplayName("return 404 NOT_FOUND when question is not found")
    public void return404NotFoundStatusCodeWhenQuestionIsNotFound() {
        ResponseEntity<QuestionWithAnswers> response = questionDsl.getQuestionById("unknown")
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
