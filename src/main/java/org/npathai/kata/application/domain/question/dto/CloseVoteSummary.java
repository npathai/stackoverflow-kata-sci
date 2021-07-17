package org.npathai.kata.application.domain.question.dto;

import lombok.Data;

@Data
public class CloseVoteSummary {
    private int castVotes;
    private int remainingVotes;
}
