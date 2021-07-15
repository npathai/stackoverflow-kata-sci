package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.CloseVoteSummary;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Question close voting feature should")
public class QuestionCloseVoteFeatureShould extends AcceptanceTestBase {
    private static final String ORIGINAL_POSTER_ID = "6";
    private static final String CLOSE_VOTER_1_ID = "1";
    private static final String CLOSE_VOTER_2_ID = "2";
    private static final String CLOSE_VOTER_3_ID = "3";
    private static final String CLOSE_VOTER_4_ID = "4";
    private static final String ANSWERER_ID = "5";

    private QuestionDsl questionDsl;
    private AnswerDsl answerDsl;
    private String questionId;

    @BeforeEach
    public void setUp() {
        questionDsl = new QuestionDsl(restTemplate);
        answerDsl = new AnswerDsl(restTemplate);

        questionId = questionDsl.aQuestion()
                .byUser(ORIGINAL_POSTER_ID)
                .exec().getId();

        CloseVoteSummary closeVoteSummary1 = questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_1_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(closeVoteSummary1.getCastVotes()).isEqualTo(1);
        assertThat(closeVoteSummary1.getRemainingVotes()).isEqualTo(3);

        CloseVoteSummary closeVoteSummary2 = questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_2_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(closeVoteSummary2.getCastVotes()).isEqualTo(2);
        assertThat(closeVoteSummary2.getRemainingVotes()).isEqualTo(2);

        CloseVoteSummary closeVoteSummary3 = questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_3_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(closeVoteSummary3.getCastVotes()).isEqualTo(3);
        assertThat(closeVoteSummary3.getRemainingVotes()).isEqualTo(1);

        CloseVoteSummary closeVoteSummary4 = questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_4_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(closeVoteSummary4.getCastVotes()).isEqualTo(4);
        assertThat(closeVoteSummary4.getRemainingVotes()).isEqualTo(0);
    }

    @Test
    @DisplayName("close the question after 4 close votes")
    public void closeTheQuestionAfterFourCloseVotes() {
        assertThat(questionDsl.getQuestionById(questionId).exec().getQuestion().getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("not allow user to post answer after question is closed")
    public void notAllowUserToPostAnswerAfterQuestionIsClosed() {
        assertThat(answerDsl.anAnswer().onQuestion(questionId).execReturningResponseEntity().getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
