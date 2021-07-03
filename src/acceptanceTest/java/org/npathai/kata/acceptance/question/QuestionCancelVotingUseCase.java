package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.npathai.kata.acceptance.vote.VotingScenarioAcceptanceTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Question cancel voting feature should")
public class QuestionCancelVotingUseCase extends AcceptanceTestBase {

    private static final String VOTER_2_ID = "1";
    public static final String ORIGINAL_POSTER_ID = "2";
    public static final String VOTER_1_ID = "3";
    private static final String VOTER_3_ID = "4";

    private QuestionDsl questionDsl;
    private UserDsl userDsl;
    private String questionId;

    @BeforeEach
    public void usersAndAQuestion() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);

        questionId = questionDsl.aQuestion()
                .byUser(ORIGINAL_POSTER_ID)
                .exec().getId();

        questionDsl.anUpVote()
                .byUser(VOTER_1_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.anUpVote()
                .byUser(VOTER_2_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.aDownVote()
                .byUser(VOTER_3_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.cancelVote()
                .byUser(VOTER_1_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.cancelVote()
                .byUser(VOTER_2_ID)
                .onQuestion(questionId)
                .exec();

        questionDsl.cancelVote()
                .byUser(VOTER_3_ID)
                .onQuestion(questionId)
                .exec();
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert question score")
    public void revertQuestionScore() {
        assertThat(questionDsl.getQuestionById(questionId).exec()
                .getQuestion().getScore()).isEqualTo(0);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert original poster reputation")
    public void revertOriginalPosterReputation() {
        assertThat(userDsl.getUserById(ORIGINAL_POSTER_ID).exec()
                .getReputation()).isEqualTo(3000);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("revert voter cast 👍 and 👎 count")
    public void revertVoterCastUpVotesAndDownVotesCount() {
        assertVoterVoteCountsUnaffected(VOTER_1_ID);
        assertVoterVoteCountsUnaffected(VOTER_2_ID);
        assertVoterVoteCountsUnaffected(VOTER_3_ID);
    }

    private void assertVoterVoteCountsUnaffected(String voterId) {
        User voter1 = userDsl.getUserById(voterId).exec();

        assertThat(voter1.getCastUpVotes()).isEqualTo(0);
        assertThat(voter1.getCastDownVotes()).isEqualTo(0);
    }
}
