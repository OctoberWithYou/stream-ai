/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import java.util.List;

/**
 * 补全提供者接口，根据当前输入缓冲区和光标位置返回补全候选项。
 *
 * <h2>设计说明</h2>
 *
 * <p>这是补全系统的核心扩展点。采用函数式接口设计，便于通过 lambda 表达式快速实现简单补全逻辑， 同时支持通过独立的实现类构建复杂的补全策略（如命令补全、参数补全、文件路径补全等）。
 *
 * <h2>扩展方式</h2>
 *
 * <ul>
 *   <li>直接实现：{@code buffer -> List.of(new CompletionCandidate("hello"))}
 *   <li>{@link CommandCompleter}：内置的命令级补全实现
 *   <li>{@link AggregateCompletionProvider}：组合多个 Provider，实现多策略补全
 *   <li>自定义实现：如基于上下文的参数补全、文件路径补全等
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <pre>{@code
 * // Lambda 方式
 * CompletionProvider provider = (buffer, cursor) ->
 *     List.of(new CompletionCandidate("help", "Show help message"));
 *
 * // 命令补全
 * CompletionProvider cmdCompleter = new CommandCompleter("help", "print", "stop", "exit");
 *
 * // 组合多个 Provider
 * CompletionProvider combined = new AggregateCompletionProvider(cmdCompleter, argCompleter);
 *
 * // 传入控制台
 * IConsole console = ConsoleFactory.create(combined);
 * }</pre>
 *
 * <h2>线程安全</h2>
 *
 * <p>实现类应保证线程安全，因为补全可能在任意线程被调用。
 *
 * @see CompletionCandidate
 * @see CommandCompleter
 * @see AggregateCompletionProvider
 * @see ConsoleFactory
 */
@FunctionalInterface
public interface CompletionProvider {

  /**
   * 根据当前输入缓冲区和光标位置，返回补全候选项列表。
   *
   * @param buffer 当前输入缓冲区的完整文本
   * @param cursor 光标在缓冲区中的位置（从 0 开始）
   * @return 补全候选项列表，不能为 {@code null}；空列表表示无可用补全
   */
  List<CompletionCandidate> complete(String buffer, int cursor);
}
