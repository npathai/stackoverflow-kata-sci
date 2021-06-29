package org.npathai.kata.acceptance.question;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.CreateQuestionResponse;
import org.npathai.kata.acceptance.question.testview.Question;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RecentQuestionsFeatureShould extends AcceptanceTestBase {

    public static final String USER_ID = "1";
    private QuestionDsl questionDsl;

    @BeforeEach
    public void setUp() {
        questionDsl = new QuestionDsl(restTemplate);
    }

    @ClearTables
    @Test
    public void returnFirstPageOfQuestionsSortedByDescendingOrderOfTimeOfCreation() {
        List<Question> questions = IntStream.range(0, 10)
                .mapToObj(this::createQuestion)
                .collect(Collectors.toList());

        Page<Question> firstPage = questionDsl.recent()
                .page(0)
                .perPage(2)
                .exec();

        assertThat(firstPage.getContent().size()).isEqualTo(2);
        for (int i = 0; i < 2; i++) {
            assertThat(firstPage.getContent().get(i)).isEqualTo(questions.get(questions.size() - 1 - i));
        }
    }

    private Question createQuestion(int i) {
        return questionDsl.create()
                .byUser(USER_ID)
                .withTitle("Question " + i)
                .withBody("Question body " + i)
                .withTags(List.of("java", String.valueOf(i)))
                .exec();
    }
}
