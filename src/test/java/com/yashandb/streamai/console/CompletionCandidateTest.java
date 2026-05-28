/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.yashandb.streamai.console.jline.CompletionCandidate;
import com.yashandb.streamai.exception.SaiUnexpectedException;
import org.junit.Test;

/** CompletionCandidate 单元测试。 */
public class CompletionCandidateTest {

  @Test
  public void constructorWithValueAndDescription() {
    final CompletionCandidate candidate = new CompletionCandidate("help", "Show help message");
    assertEquals("help", candidate.getValue());
    assertEquals("Show help message", candidate.getDescription());
  }

  @Test
  public void constructorWithValueOnly() {
    final CompletionCandidate candidate = new CompletionCandidate("exit");
    assertEquals("exit", candidate.getValue());
    assertNull(candidate.getDescription());
  }

  @Test(expected = SaiUnexpectedException.class)
  public void constructorWithNullValueThrows() {
    new CompletionCandidate(null);
  }

  @Test(expected = SaiUnexpectedException.class)
  public void constructorWithNullValueAndDescriptionThrows() {
    new CompletionCandidate(null, "description");
  }

  @Test
  public void constructorWithNullDescription() {
    final CompletionCandidate candidate = new CompletionCandidate("test", null);
    assertEquals("test", candidate.getValue());
    assertNull(candidate.getDescription());
  }
}
