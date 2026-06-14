/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import com.yashandb.streamai.exception.SaiIllegalArgumentException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合补全提供者，将多个 {@link CompletionProvider} 的结果合并返回。
 *
 * <h2>设计说明</h2>
 *
 * <p>{@code AggregateCompletionProvider} 实现组合模式，按照注册顺序依次调用各 Provider，将所有非空结果合并为一个列表。
 * 适用于需要同时支持多种补全策略的场景（如命令补全 + 参数补全）。
 *
 * <h2>实现细节</h2>
 *
 * <ul>
 *   <li><b>调用顺序</b>：按照构造时传入的顺序依次调用各 Provider
 *   <li><b>结果合并</b>：将所有 Provider 返回的候选项合并为一个列表，不去重
 *   <li><b>返回不可变</b>：返回的列表通过 {@code Collections.unmodifiableList} 包装，防止外部修改
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <pre>{@code
 * CompletionProvider cmdCompleter = new CommandCompleter("help", "echo", "exit");
 * CompletionProvider argCompleter = (buffer, cursor) -> {
 *   if (buffer.startsWith("echo ")) {
 *     return List.of(new CompletionCandidate("hello"), new CompletionCandidate("world"));
 *   }
 *   return Collections.emptyList();
 * };
 * CompletionProvider combined = new AggregateCompletionProvider(cmdCompleter, argCompleter);
 * IConsole console = ConsoleFactory.create(combined);
 * }</pre>
 *
 * <h2>线程安全</h2>
 *
 * <p>Provider 列表在构造时通过 {@code List.of()} 创建不可变副本，因此是线程安全的。但各 Provider 实现本身需保证线程安全。
 *
 * @see CompletionProvider
 * @see CommandCompleter
 */
public final class AggregateCompletionProvider implements CompletionProvider {

  private final List<CompletionProvider> providers;

  /**
   * 构造聚合补全提供者。
   *
   * @param providers 要聚合的补全提供者列表（会被防御性拷贝）
   * @throws SaiIllegalArgumentException 当 providers 为 null 时
   */
  public AggregateCompletionProvider(final CompletionProvider... providers)
      throws SaiIllegalArgumentException {
    if (providers == null) {
      throw new SaiIllegalArgumentException("Providers must not be null");
    }
    this.providers = List.of(providers);
  }

  @Override
  public List<CompletionCandidate> complete(final String buffer, final int cursor) {
    final List<CompletionCandidate> result = new ArrayList<>();
    for (final CompletionProvider provider : providers) {
      result.addAll(provider.complete(buffer, cursor));
    }
    return Collections.unmodifiableList(result);
  }
}
