/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import com.yashandb.streamai.exception.SaiUnexpectedException;

/**
 * 补全候选项，表示一个可被用户选择的补全建议。
 *
 * <h2>设计说明</h2>
 *
 * <p>{@code CompletionCandidate} 是补全系统的基本单元。当用户按下 Tab 键时，{@link CompletionProvider} 返回一组候选项，
 * 每个候选项代表一个可能的补全选择。实现层（如 {@link CliConsole}）负责将这些候选项转换为终端可理解的格式（如 JLine 的 {@code Candidate} 对象）。
 *
 * <h2>字段说明</h2>
 *
 * <ul>
 *   <li>{@code value} - 补全文本，将被插入到输入缓冲区中。不能为 {@code null}
 *   <li>{@code description} - 候选项的简要说明，用于在补全列表中显示。可为 {@code null}
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <pre>{@code
 * // 无描述
 * CompletionCandidate simple = new CompletionCandidate("help");
 *
 * // 带描述
 * CompletionCandidate detailed = new CompletionCandidate("quit", "Exit the application");
 * }</pre>
 *
 * <h2>线程安全</h2>
 *
 * <p>不可变对象，线程安全。
 *
 * @see CompletionProvider
 * @see CommandCompleter
 */
public final class CompletionCandidate {

  private final String value;
  private final String description;

  /**
   * 构造带描述的补全候选项。
   *
   * @param value 补全文本（不能为 {@code null}）
   * @param description 候选项描述（可为 {@code null}）
   * @throws SaiUnexpectedException 当 value 为 null 时
   */
  public CompletionCandidate(final String value, final String description) {
    if (value == null) {
      throw new SaiUnexpectedException("Completion candidate value must not be null");
    }
    this.value = value;
    this.description = description;
  }

  /**
   * 构造无描述的补全候选项。
   *
   * @param value 补全文本（不能为 {@code null}）
   * @throws SaiUnexpectedException 当 value 为 null 时
   */
  public CompletionCandidate(final String value) {
    this(value, null);
  }

  /**
   * 返回补全文本。
   *
   * @return 补全文本，不为 null
   */
  public String getValue() {
    return value;
  }

  /**
   * 返回候选项描述。
   *
   * @return 描述文本，可能为 null
   */
  public String getDescription() {
    return description;
  }
}
