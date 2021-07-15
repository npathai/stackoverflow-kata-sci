package org.npathai.kata.acceptance.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.question.dsl.AnswerDsl;
import org.npathai.kata.acceptance.question.dsl.QuestionDsl;
import org.npathai.kata.acceptance.question.testview.Answer;
import org.npathai.kata.acceptance.question.testview.QuestionWithAnswers;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.npathai.kata.acceptance.vote.VotingScenarioAcceptanceTest;
import org.npathai.kata.acceptance.vote.testview.Score;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Answer voting feature should")
public class AnswerVotingFeatureShould extends AcceptanceTestBase {

    private static final String VOTER_2_ID = "1";
    public static final String ORIGINAL_POSTER_ID = "2";
    public static final String VOTER_1_ID = "3";
    private static final String DOWN_VOTER_ID = "4";
    private static final String ANSWERER_ID = "5";
    private static final String INSUFFICIENT_REP_VOTER_ID = "6";
    private static final String NEW_USER_ID = "6";

    private QuestionDsl questionDsl;
    private UserDsl userDsl;
    private String questionId;
    private String answerId;
    private AnswerDsl answerDsl;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        questionDsl = new QuestionDsl(restTemplate);
        answerDsl = new AnswerDsl(restTemplate);

        questionId = questionDsl.aQuestion()
                .byUser(ORIGINAL_POSTER_ID)
                .exec().getId();

        answerId = answerDsl.anAnswer()
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
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update answer score")
    public void updateAnswerScore() {
        QuestionWithAnswers questionWithAnswers = questionDsl
                .getQuestionById(questionId)
                .exec();

        Answer answer = questionWithAnswers.getAnswers().get(0);
        assertThat(answer.getScore()).isEqualTo(1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update original poster reputation")
    public void updateOriginalPosterReputation() {
        User originalPoster = userDsl.getUserById(ANSWERER_ID).exec();

        assertThat(originalPoster.getReputation()).isEqualTo(3015);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("update voter cast 👍 and 👎 count")
    public void updateVoterCastUpVotesAndDownVotesCount() {
        assertVoterVoteCount(VOTER_1_ID, 1, 0);
        assertVoterVoteCount(VOTER_2_ID, 1, 0);
        assertVoterVoteCount(DOWN_VOTER_ID, 0, 1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("reduce voter reputation on 👎")
    public void reduceVoterReputationOnDownVote() {
        User downVoter = userDsl.getUserById(DOWN_VOTER_ID).exec();

        assertThat(downVoter.getReputation()).isEqualTo(2999);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow user with insufficient reputation to cast 👍")
    public void notAllowUserWithInsufficientReputationToVote() {
        ResponseEntity<Score> response = answerDsl.anUpVote()
                .byUser(INSUFFICIENT_REP_VOTER_ID)
                .onAnswer(questionId, answerId)
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(questionDsl.getQuestionById(questionId).exec().getAnswers().get(0).getScore()).isEqualTo(1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not allow original poster cast 👍 on own answer")
    public void notAllowOriginalPosterToVoteOnOwnAnswer() {
        ResponseEntity<Score> response = answerDsl.anUpVote()
                .byUser(ANSWERER_ID)
                .onAnswer(questionId, answerId)
                .execReturningResponseEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(questionDsl.getQuestionById(questionId).exec().getAnswers().get(0).getScore()).isEqualTo(1);
    }

    @VotingScenarioAcceptanceTest
    @DisplayName("not drop original poster reputation to drop below 1")
    public void notDropOriginalPosterReputationBelowOneOnDownVote() {
        Answer newUserAnswer = answerDsl.anAnswer()
                .byUser(NEW_USER_ID)
                .onQuestion(questionId)
                .exec();

        answerDsl.aDownVote()
                .byUser(VOTER_1_ID)
                .onAnswer(questionId, newUserAnswer.getId())
                .exec();

        assertThat(userDsl.getUserById(NEW_USER_ID).exec().getReputation()).isEqualTo(1);
    }

    private void assertVoterVoteCount(String voterId, int upVoteCount, int downVoteCount) {
        User voter1 = userDsl.getUserById(voterId).exec();

        assertThat(voter1.getCastUpVotes()).isEqualTo(upVoteCount);
        assertThat(voter1.getCastDownVotes()).isEqualTo(downVoteCount);
    }

}
