package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.CloseVoteSummary;
import org.npathai.kata.acceptance.vote.VotingScenarioAcceptanceTest;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Question reopen voting feature should")
public class QuestionReopenVoteUseCaseShould extends AcceptanceTestBase {

    private static final String ORIGINAL_POSTER_ID = "6";
    private static final String CLOSE_VOTER_1_ID = "1";
    private static final String CLOSE_VOTER_2_ID = "2";
    private static final String CLOSE_VOTER_3_ID = "3";
    private static final String CLOSE_VOTER_4_ID = "4";
    private static final String CLOSE_VOTER_5_ID = "5";
    private static final String INSUFFICIENT_REP_VOTER = "6";

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

        questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_1_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_2_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_3_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.aCloseVote()
                .byUser(CLOSE_VOTER_4_ID)
                .onQuestion(questionId)
                .exec();

        CloseVoteSummary reopenVoteSummary1 = questionDsl.aReopenVote()
                .byUser(CLOSE_VOTER_4_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(reopenVoteSummary1.getCastVotes()).isEqualTo(1);
        assertThat(reopenVoteSummary1.getRemainingVotes()).isEqualTo(3);

        CloseVoteSummary reopenVoteSummary2 = questionDsl.aReopenVote()
                .byUser(CLOSE_VOTER_1_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(reopenVoteSummary2.getCastVotes()).isEqualTo(2);
        assertThat(reopenVoteSummary2.getRemainingVotes()).isEqualTo(2);

        CloseVoteSummary reopenVoteSummary3 = questionDsl.aReopenVote()
                .byUser(CLOSE_VOTER_2_ID)
                .onQuestion(questionId)
                .exec();
        assertThat(reopenVoteSummary3.getCastVotes()).isEqualTo(3);
        assertThat(reopenVoteSummary3.getRemainingVotes()).isEqualTo(1);

        CloseVoteSummary reopenVoteSummary4 = questionDsl.aReopenVote()
                .byUser(CLOSE_VOTER_3_ID)
                .onQuestion(questionId)
                .exec();

        assertThat(reopenVoteSummary4.getCastVotes()).isEqualTo(4);
        assertThat(reopenVoteSummary4.getRemainingVotes()).isEqualTo(0);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("close the question after 4 reopen votes")
    public void reopensTheQuestionAfterFourReopenVotes() {
        assertThat(questionDsl.getQuestionById(questionId).exec().getQuestion().getClosedAt()).isNull();
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("again allows user to post answer after question is reopened")
    public void allowsUserToPostAnswerAfterQuestionIsReopened() {
        assertThat(answerDsl.anAnswer().byUser(CLOSE_VOTER_4_ID).onQuestion(questionId)
                .execReturningResponseEntity().getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow user with insufficient reputation to vote")
    public void notAllowUserWithInsufficientReputationToReopenVote() {
        assertThat(questionDsl.aReopenVote()
                .byUser(INSUFFICIENT_REP_VOTER)
                .onQuestion(questionId)
                .execReturningResponseEntity()
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
