package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.npathai.kata.acceptance.user.dsl.UserDsl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RecentQuestionsFeatureShould extends AcceptanceTestBase {

    private QuestionDsl questionDsl;
    private String opId;
    private UserDsl userDsl;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        opId = userDsl.registerUser()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec().getId();

        String answerer1 = userDsl.registerUser()
                .withUsername("harry")
                .withEmail("harry@gmail.com")
                .exec().getId();

        String answerer2 = userDsl.registerUser()
                .withUsername("ron")
                .withEmail("ron@gmail.com")
                .exec().getId();

        String questionId = questionDsl.aQuestion()
                .byUser(opId)
                .withTitle("Title")
                .withBody("Body")
                .withTags(List.of("java"))
                .exec().getId();

        questionDsl.anAnswer()
                .byUser(answerer1)
                .onQuestion(questionId)
                .withBody("Answer Body 1")
                .exec();

        questionDsl.anAnswer()
                .byUser(answerer2)
                .onQuestion(questionId)
                .withBody("Answer Body 2")
                .exec();
    }

    @ClearTables
    @Test
    public void returnBasicQuestionInformation() {
        Page<Question> questionPage = questionDsl.recent().exec();
        assertThat(questionPage.getContent().get(0))
                .satisfies(q -> {
                    assertThat(q.getId()).isNotBlank();
                    assertThat(q.getAuthorId()).isEqualTo(opId);
                    assertThat(q.getTitle()).isEqualTo("Title");
                    assertThat(q.getTags()).map(Tag::getName)
                            .containsExactlyInAnyOrderElementsOf(List.of("java"));
                    assertThat(q.getAnswerCount()).isEqualTo(2);
                });
    }

    @ClearTables
    @Test
    public void returnFirstTenQuestionsSortedByDescendingOrderOfTimeOfCreation() {
        List<Question> questions = IntStream.range(0, 20)
                .mapToObj(this::postQuestion)
                .collect(Collectors.toList());

        Page<Question> firstPage = questionDsl.recent().exec();
        int pageSize = 10;
        assertThat(firstPage.getContent().size()).isEqualTo(pageSize);
        for (int i = 0; i < pageSize; i++) {
            assertThat(firstPage.getContent().get(i).getId())
                    .isEqualTo(questions.get(questions.size() - 1 - i).getId());
        }
    }

    private Question postQuestion(int i) {
        return questionDsl.aQuestion()
                .byUser(opId)
                .withTitle("Question " + i)
                .withBody("Question body " + i)
                .withTags(List.of(String.valueOf(i), "java"))
                .exec();
    }
}
