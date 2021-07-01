package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Answer;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.question.testview.QuestionWithAnswers;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ViewQuestionFeatureShould extends AcceptanceTestBase {

    private UserDsl userDsl;
    private QuestionDsl questionDsl;
    private User op;
    private Question question;
    private User answerer1;
    private User answerer2;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        op = userDsl.registerUser()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec();

        answerer1 = userDsl.registerUser()
                .withUsername("harry")
                .withEmail("harry@gmail.com")
                .exec();

        answerer2 = userDsl.registerUser()
                .withUsername("alice")
                .withEmail("alice@gmail.com")
                .exec();

        question = questionDsl.aQuestion()
                .byUser(op.getId())
                .withTitle("Question")
                .withBody("Question body")
                .withTags(List.of("kata", "java"))
                .exec();
    }

    @Test
    public void returnQuestionWithoutAnswersWhenUnanswered() {
        QuestionWithAnswers questionWithAnswers = questionDsl.view(question.getId()).exec();

        assertThat(questionWithAnswers.getQuestion()).isEqualTo(question);
        assertThat(questionWithAnswers.getAnswers()).isEmpty();
    }

    @Test
    public void returnQuestionWithAllAnswers() {
        Answer answer1 = questionDsl.anAnswer()
                .byUser(answerer1.getId())
                .onQuestion(question.getId())
                .withBody("This is answer 1")
                .exec();

        Answer answer2 = questionDsl.anAnswer()
                .byUser(answerer2.getId())
                .onQuestion(question.getId())
                .withBody("This is answer 2")
                .exec();

        QuestionWithAnswers questionWithAnswers = questionDsl.view(question.getId()).exec();
        assertThat(questionWithAnswers.getQuestion()).isEqualTo(question);
        assertThat(questionWithAnswers.getAnswers()).containsExactlyInAnyOrder(answer1, answer2);
    }

    @Test
    public void return404NotFoundStatusCodeWhenQuestionIsNotFound() {
        ResponseEntity<QuestionWithAnswers> response = questionDsl.view("unknown")
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
