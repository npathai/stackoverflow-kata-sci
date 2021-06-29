package org.npathai.kata.acceptance.base.testview;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    List<T> content;
}
