/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import com.yashandb.streamai.exception.SaiIllegalArgumentException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令级补全提供者，根据已注册的命令列表进行前缀匹配补全。
 *
 * <h2>设计说明</h2>
 *
 * <p>{@code CommandCompleter} 是最常用的补全实现，适用于顶层命令补全场景。当用户在输入命令的前几个字符后按下 Tab 键时， 返回所有以当前输入为前缀的命令作为候选项。
 *
 * <h2>实现细节</h2>
 *
 * <ul>
 *   <li><b>前缀匹配</b>：从缓冲区起始位置到光标位置的文本作为前缀，与已注册命令进行 {@code startsWith} 匹配
 *   <li><b>空格处理</b>：如果前缀中包含空格（表示已输入命令名），则返回空列表（不支持子命令补全）
 *   <li><b>大小写敏感</b>：匹配是大小写敏感的，但命令本身通常使用小写
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <pre>{@code
 * CommandCompleter completer = new CommandCompleter("help", "print", "stop", "echo", "exit");
 * IConsole console = ConsoleFactory.create(completer);
 * }</pre>
 *
 * <h2>扩展说明</h2>
 *
 * <p>如需更复杂的命令补全（如子命令、参数补全），可通过 {@link AggregateCompletionProvider} 将本类与其他 {@link
 * CompletionProvider} 组合使用。
 *
 * <h2>线程安全</h2>
 *
 * <p>命令列表在构造时通过 {@code List.of()} 创建不可变副本，因此是线程安全的。
 *
 * @see CompletionProvider
 * @see AggregateCompletionProvider
 */
public final class CommandCompleter implements CompletionProvider {

  private final List<String> commands;

  /**
   * 构造命令补全器。
   *
   * @param commands 支持的命令列表（会被防御性拷贝）
   * @throws SaiIllegalArgumentException 当 commands 为 null 或包含 null 元素时
   */
  public CommandCompleter(final String... commands) throws SaiIllegalArgumentException {
    if (commands == null) {
      throw new SaiIllegalArgumentException("Commands must not be null");
    }
    this.commands = List.of(commands);
  }

  @Override
  public List<CompletionCandidate> complete(final String buffer, final int cursor) {
    final String input = buffer.substring(0, cursor);
    if (input.contains(" ")) {
      return Collections.emptyList();
    }
    final String prefix = input.trim();
    final List<CompletionCandidate> candidates = new ArrayList<>();
    for (final String cmd : commands) {
      if (cmd.startsWith(prefix)) {
        candidates.add(new CompletionCandidate(cmd));
      }
    }
    return candidates;
  }
}
