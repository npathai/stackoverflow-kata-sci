package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RecentQuestionsFeatureShould extends AcceptanceTestBase {

    public static final String USER_ID = "1";
    private QuestionDsl questionDsl;
    private User user;

    @BeforeEach
    public void setUp() {
        UserDsl userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        user = userDsl.create()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec();

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
            assertThat(firstPage.getContent().get(i)).isEqualTo(questions.get(questions.size() - 1 - i));
        }
    }

    private Question postQuestion(int i) {
        return questionDsl.post()
                .byUser(user.getId())
                .withTitle("Question " + i)
                .withBody("Question body " + i)
                .withTags(List.of("java", String.valueOf(i)))
                .exec();
    }
}
