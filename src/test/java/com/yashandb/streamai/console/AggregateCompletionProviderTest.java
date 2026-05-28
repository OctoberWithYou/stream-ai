/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.yashandb.streamai.console.jline.AggregateCompletionProvider;
import com.yashandb.streamai.console.jline.CompletionCandidate;
import com.yashandb.streamai.console.jline.CompletionProvider;
import com.yashandb.streamai.exception.SaiIllegalArgumentException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** AggregateCompletionProvider 单元测试。 */
public class AggregateCompletionProviderTest {

  @Test
  public void aggregateEmptyProviders() throws SaiIllegalArgumentException {
    final AggregateCompletionProvider provider = new AggregateCompletionProvider();
    final List<CompletionCandidate> candidates = provider.complete("test", 4);
    assertTrue(candidates.isEmpty());
  }

  @Test
  public void aggregateSingleProvider() throws SaiIllegalArgumentException {
    final CompletionProvider mock = (buffer, cursor) -> List.of(new CompletionCandidate("result"));
    final AggregateCompletionProvider provider = new AggregateCompletionProvider(mock);
    final List<CompletionCandidate> candidates = provider.complete("test", 4);
    assertEquals(1, candidates.size());
    assertEquals("result", candidates.get(0).getValue());
  }

  @Test
  public void aggregateMultipleProviders() throws SaiIllegalArgumentException {
    final CompletionProvider provider1 =
        (buffer, cursor) -> List.of(new CompletionCandidate("a"), new CompletionCandidate("b"));
    final CompletionProvider provider2 = (buffer, cursor) -> List.of(new CompletionCandidate("c"));
    final AggregateCompletionProvider provider =
        new AggregateCompletionProvider(provider1, provider2);
    final List<CompletionCandidate> candidates = provider.complete("test", 4);
    assertEquals(3, candidates.size());
    assertEquals("a", candidates.get(0).getValue());
    assertEquals("b", candidates.get(1).getValue());
    assertEquals("c", candidates.get(2).getValue());
  }

  @Test
  public void aggregateWithEmptyProvider() throws SaiIllegalArgumentException {
    final CompletionProvider provider1 = (buffer, cursor) -> List.of(new CompletionCandidate("a"));
    final CompletionProvider provider2 = (buffer, cursor) -> Collections.emptyList();
    final CompletionProvider provider3 = (buffer, cursor) -> List.of(new CompletionCandidate("b"));
    final AggregateCompletionProvider provider =
        new AggregateCompletionProvider(provider1, provider2, provider3);
    final List<CompletionCandidate> candidates = provider.complete("test", 4);
    assertEquals(2, candidates.size());
    assertEquals("a", candidates.get(0).getValue());
    assertEquals("b", candidates.get(1).getValue());
  }

  @Test(expected = SaiIllegalArgumentException.class)
  public void constructorWithNullThrows() throws SaiIllegalArgumentException {
    new AggregateCompletionProvider((CompletionProvider[]) null);
  }

  @Test
  public void resultIsUnmodifiable() throws SaiIllegalArgumentException {
    final CompletionProvider mock = (buffer, cursor) -> List.of(new CompletionCandidate("result"));
    final AggregateCompletionProvider provider = new AggregateCompletionProvider(mock);
    final List<CompletionCandidate> candidates = provider.complete("test", 4);
    try {
      candidates.add(new CompletionCandidate("should fail"));
      throw new AssertionError("Should have thrown UnsupportedOperationException");
    } catch (final UnsupportedOperationException e) {
      // expected
    }
  }
}
