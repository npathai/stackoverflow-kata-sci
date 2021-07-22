package org.npathai.kata.application.domain.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteSummary {
    private int castVotes;
    private int remainingVotes;
}
