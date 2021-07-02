package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.npathai.kata.acceptance.vote.VotingScenarioAcceptanceTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Answer cancel voting feature should")
public class AnswerCancelVotingFeatureShould extends AcceptanceTestBase {

    private static final String VOTER_2_ID = "1";
    public static final String ORIGINAL_POSTER_ID = "2";
    public static final String VOTER_1_ID = "3";
    private static final String DOWN_VOTER_ID = "4";
    private static final String ANSWERER_ID = "5";

    private QuestionDsl questionDsl;
    private UserDsl userDsl;
    private String questionId;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        AnswerDsl answerDsl = new AnswerDsl(restTemplate);

        questionId = questionDsl.aQuestion()
                .byUser(ORIGINAL_POSTER_ID)
                .exec().getId();

        String answerId = answerDsl.anAnswer()
                .byUser(ANSWERER_ID)
                .onQuestion(questionId)
                .exec().getId();

        answerDsl.anUpVote()
                .byUser(VOTER_1_ID)
                .onAnswer(questionId, answerId)
                .exec();

        answerDsl.anUpVote()
                .byUser(VOTER_2_ID)
                .onAnswer(questionId, answerId)
                .exec();

        answerDsl.aDownVote()
                .byUser(DOWN_VOTER_ID)
                .onAnswer(questionId, answerId)
                .exec();

        answerDsl.cancelVote()
                .byUser(VOTER_1_ID)
                .onAnswer(questionId, answerId)
                .exec();

        answerDsl.cancelVote()
                .byUser(VOTER_2_ID)
                .onAnswer(questionId, answerId)
                .exec();

        answerDsl.cancelVote()
                .byUser(DOWN_VOTER_ID)
                .onAnswer(questionId, answerId)
                .exec();
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert score of answer")
    public void revertAnswerScore() {
        assertThat(questionDsl.getQuestionById(questionId).exec().getAnswers().get(0).getScore()).isEqualTo(0);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert revert original poster reputation")
    public void revertOriginalPosterReputation() {
        assertThat(userDsl.getUserById(ORIGINAL_POSTER_ID).exec()
                .getReputation()).isEqualTo(3000);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert cast 👍 and 👎 count")
    public void revertVoterCastUpVotesAndDownVotesCount() {
        assertVoterVoteCountsUnaffected(VOTER_1_ID);
        assertVoterVoteCountsUnaffected(VOTER_2_ID);
        assertVoterVoteCountsUnaffected(DOWN_VOTER_ID);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert voter reputation on cancelling 👎")
    public void revertVoterReputationOnCancellingDownVote() {
        User downVoter = userDsl.getUserById(DOWN_VOTER_ID).exec();

        assertThat(downVoter.getReputation()).isEqualTo(3000);
    }

    private void assertVoterVoteCountsUnaffected(String voterId) {
        User voter1 = userDsl.getUserById(voterId).exec();

        assertThat(voter1.getCastUpVotes()).isEqualTo(0);
        assertThat(voter1.getCastDownVotes()).isEqualTo(0);
    }
}
