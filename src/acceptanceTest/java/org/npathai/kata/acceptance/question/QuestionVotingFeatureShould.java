package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.question.testview.QuestionWithAnswers;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.npathai.kata.acceptance.vote.VotingScenarioAcceptanceTest;
import org.npathai.kata.acceptance.vote.testview.Score;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Question voting feature should")
public class QuestionVotingFeatureShould extends AcceptanceTestBase {

    private static final String VOTER_2_ID = "1";
    public static final String ORIGINAL_POSTER_ID = "2";
    public static final String VOTER_1_ID = "3";
    private static final String VOTER_3_ID = "4";
    private static final String INSUFFICIENT_UP_VOTE_REP_VOTER_ID = "7";
    private static final String INSUFFICIENT_DOWN_VOTE_REP_VOTER_ID = "8";
    private static final String NEW_USER_ID = "6";

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
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update question score")
    public void updateQuestionScore() {
        QuestionWithAnswers updatedQuestion = questionDsl
                .getQuestionById(questionId)
                .exec();

        assertThat(updatedQuestion.getQuestion().getScore()).isEqualTo(1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update original poster reputation")
    public void updateOriginalPosterReputation() {
        User originalPoster = userDsl.getUserById(ORIGINAL_POSTER_ID).exec();

        assertThat(originalPoster.getReputation()).isEqualTo(3015);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update voter cast 👍 and 👎 count")
    public void updateVoterCastUpVotesAndDownVotesCount() {
        assertVoterVoteCount(VOTER_1_ID, 1, 0);
        assertVoterVoteCount(VOTER_2_ID, 1, 0);
        assertVoterVoteCount(VOTER_3_ID, 0, 1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow user with insufficient reputation to cast 👍")
    public void notAllowUserWithInsufficientReputationToUpVote() {
        ResponseEntity<Score> response = questionDsl.anUpVote()
                .byUser(INSUFFICIENT_UP_VOTE_REP_VOTER_ID)
                .onQuestion(questionId)
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(questionDsl.getQuestionById(questionId).exec().getQuestion().getScore()).isEqualTo(0);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow user with insufficient reputation to cast 👎")
    public void notAllowUserWithInsufficientReputationToDownVote() {
        ResponseEntity<Score> response = questionDsl.aDownVote()
                .byUser(INSUFFICIENT_DOWN_VOTE_REP_VOTER_ID)
                .onQuestion(questionId)
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(questionDsl.getQuestionById(questionId).exec().getQuestion().getScore()).isEqualTo(0);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow original poster to cast 👍 on own question")
    public void notAllowOriginalPosterToVoteOnOwnQuestion() {
        ResponseEntity<Score> response = questionDsl.anUpVote()
                .byUser(ORIGINAL_POSTER_ID)
                .onQuestion(questionId)
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(questionDsl.getQuestionById(questionId).exec().getQuestion().getScore()).isEqualTo(1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not drop original poster reputation to drop below 1")
    public void notDropOriginalPosterReputationBelowOneOnDownVote() {
        Question newUserQuestion = questionDsl.aQuestion()
                .byUser(NEW_USER_ID)
                .exec();

        questionDsl.aDownVote()
                .byUser(VOTER_1_ID)
                .onQuestion(newUserQuestion.getId())
                .exec();

        assertThat(userDsl.getUserById(NEW_USER_ID).exec().getReputation()).isEqualTo(1);
    }

    private void assertVoterVoteCount(String voterId, int upVoteCount, int downVoteCount) {
        User voter1 = userDsl.getUserById(voterId).exec();

        assertThat(voter1.getCastUpVotes()).isEqualTo(upVoteCount);
        assertThat(voter1.getCastDownVotes()).isEqualTo(downVoteCount);
    }
}
