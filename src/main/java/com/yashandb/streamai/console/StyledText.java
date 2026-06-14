/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console;

import com.yashandb.streamai.console.jline.CliConsole;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 不可变的带样式富文本对象，用于控制台输出。
 *
 * <h2>设计目标</h2>
 *
 * <p>{@code StyledText} 是控制台层的富文本抽象，独立于任何第三方终端库（如 JLine）。它封装了文本内容和样式信息， 由实现层（如 {@link
 * CliConsole}）负责转换为具体的终端输出格式。这种设计使得 {@link IConsole} 接口不暴露任何第三方依赖， 便于替换实现、单元测试和 mock。
 *
 * <h2>支持的样式</h2>
 *
 * <p>每个文本段（{@link Segment}）可独立设置以下样式：
 *
 * <ul>
 *   <li><b>前景色</b>：8 种标准终端颜色（{@link Color#BLACK}, {@link Color#RED}, {@link Color#GREEN}, {@link
 *       Color#YELLOW}, {@link Color#BLUE}, {@link Color#MAGENTA}, {@link Color#CYAN}, {@link
 *       Color#WHITE}）
 *   <li><b>文本修饰</b>：加粗（{@link Builder#bold()}）、下划线（{@link Builder#underline()}）、 斜体（{@link
 *       Builder#italic()}）、删除线（{@link Builder#strikethrough()}）
 *   <li><b>组合样式</b>：颜色和修饰可自由组合，例如"红色加粗"或"青色下划线斜体"
 * </ul>
 *
 * <h2>内部结构</h2>
 *
 * <p>{@code StyledText} 内部由若干 {@link Segment}（文本段）组成，每个段包含一段纯文本和对应的样式信息。 调用 {@link
 * Builder#append(String)} 时，会以当前样式快照创建一个新的段并追加到列表中。 构建完成后，{@code StyledText} 不可变（所有字段均为 {@code
 * final}，列表通过 {@link List#copyOf} 深拷贝）， 因此是线程安全的，可在多线程环境中自由共享。
 *
 * <h2>使用方式</h2>
 *
 * <p>通过 {@link Builder} 流式 API 构建，支持链式调用。典型用法：
 *
 * <pre>{@code
 * // 单一样式
 * StyledText error = StyledText.builder()
 *     .red().bold()
 *     .append("[ERROR] Connection failed")
 *     .build();
 *
 * // 混合样式
 * StyledText info = StyledText.builder()
 *     .bold().cyan().append("[INFO]")
 *     .normal().append(" Server started on port ")
 *     .green().append("8080")
 *     .build();
 *
 * // 纯文本（无样式）
 * StyledText plain = StyledText.of("Hello, world!");
 *
 * // 输出到控制台
 * console.print(info);
 * }</pre>
 *
 * <h2>与终端的兼容性</h2>
 *
 * <p>样式的实际渲染效果取决于终端支持。大多数现代终端支持 8 色前景和加粗；斜体、下划线、删除线在部分终端中可能不可用或被忽略。 实现层会尽力将样式映射为终端可理解的格式（如 ANSI
 * 转义序列），不支持的样式会被静默忽略。
 *
 * @see IConsole#print(StyledText)
 * @see CliConsole
 */
public final class StyledText {

  /**
   * 标准终端前景颜色枚举。
   *
   * <p>支持 8 种标准 ANSI 颜色，对应终端的 3-bit 颜色码。实际显示效果取决于终端配色方案。
   */
  public enum Color {
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE
  }

  /**
   * 文本修饰选项。
   *
   * <p>支持的修饰包括加粗、下划线、斜体、删除线。多个修饰可同时应用于同一文本段。
   */
  public enum StyleOption {
    BOLD,
    UNDERLINE,
    ITALIC,
    STRIKETHROUGH
  }

  /**
   * 文本段：包含一段纯文本及其样式信息。
   *
   * <p>每个 {@code Segment} 对应 Builder 中一次 {@code append()} 调用时的样式快照。段本身不可变。
   */
  public static final class Segment {
    private final String text;
    private final Color color;
    private final EnumSet<StyleOption> options;

    /**
     * 构造文本段。
     *
     * @param text 纯文本
     * @param color 前景色（可为 null）
     * @param options 修饰选项集合
     */
    public Segment(final String text, final Color color, final EnumSet<StyleOption> options) {
      this.text = text;
      this.color = color;
      this.options = EnumSet.copyOf(options);
    }

    /**
     * 返回文本内容。
     *
     * @return 纯文本
     */
    public String getText() {
      return text;
    }

    /**
     * 返回前景色。
     *
     * @return 前景色，可能为 null
     */
    public Color getColor() {
      return color;
    }

    /**
     * 返回修饰选项集合。
     *
     * @return 修饰选项
     */
    public EnumSet<StyleOption> getOptions() {
      return options;
    }
  }

  private final List<Segment> segments;

  private StyledText(final List<Segment> segments) {
    this.segments = List.copyOf(segments);
  }

  /**
   * 从纯文本创建无样式的 {@code StyledText}。
   *
   * <p>这是快速创建纯文本输出的便捷方法，等价于 {@code StyledText.builder().append(text).build()}。
   *
   * @param text 纯文本（不能为 {@code null}）
   * @return 无样式的 StyledText 实例
   */
  public static StyledText of(final String text) {
    return builder().append(text).build();
  }

  /**
   * 创建新的 {@link Builder} 用于构建带样式的文本。
   *
   * @return 新的构建器实例
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * 返回所有文本段。
   *
   * <p>供实现层（如 {@link CliConsole}）遍历并转换为终端输出格式。
   *
   * @return 不可变的文本段列表
   */
  public List<Segment> getSegments() {
    return segments;
  }

  /**
   * 返回去除所有样式的纯文本。
   *
   * <p>将内部所有文本段按顺序拼接，不包含任何颜色或修饰信息。常用于日志记录、测试断言等只需要纯文本内容的场景。
   *
   * @return 所有文本段拼接后的纯文本字符串
   */
  public String toPlainText() {
    final StringBuilder sb = new StringBuilder();
    for (final Segment seg : segments) {
      sb.append(seg.text);
    }
    return sb.toString();
  }

  /**
   * {@link StyledText} 的流式构建器，支持链式调用设置样式并追加文本段。
   *
   * <h3>使用模式</h3>
   *
   * <ol>
   *   <li>调用 {@link StyledText#builder()} 获取构建器实例
   *   <li>使用颜色方法（如 {@link #red()}, {@link #cyan()}）设置前景色
   *   <li>使用修饰方法（如 {@link #bold()}, {@link #underline()}）启用文本修饰
   *   <li>调用 {@link #append(String)} 以当前样式追加文本段
   *   <li>可重复步骤 2-4 构建多段混合样式文本
   *   <li>调用 {@link #normal()} 重置为默认样式（无颜色、无修饰）
   *   <li>最后调用 {@link #build()} 获取不可变的 {@link StyledText} 实例
   * </ol>
   *
   * <p>示例：
   *
   * <pre>{@code
   * StyledText text = StyledText.builder()
   *     .bold().red().append("ERROR: ")      // 红色加粗
   *     .normal().append("File not found: ") // 默认样式
   *     .underline().append("/etc/config")   // 下划线
   *     .build();
   * }</pre>
   */
  public static final class Builder {
    private final List<Segment> segments = new ArrayList<>();
    private Color color;
    private final EnumSet<StyleOption> options = EnumSet.noneOf(StyleOption.class);

    private Builder() {}

    /**
     * 设置前景颜色。
     *
     * @param c 颜色（{@code null} 表示使用终端默认颜色）
     * @return this
     */
    public Builder color(final Color c) {
      this.color = c;
      return this;
    }

    /**
     * 设置前景色为黑色。
     *
     * @return this
     */
    public Builder black() {
      return color(Color.BLACK);
    }

    /**
     * 设置前景色为红色。常用于错误、警告信息。
     *
     * @return this
     */
    public Builder red() {
      return color(Color.RED);
    }

    /**
     * 设置前景色为绿色。常用于成功、正常状态信息。
     *
     * @return this
     */
    public Builder green() {
      return color(Color.GREEN);
    }

    /**
     * 设置前景色为黄色。常用于警告、提示信息。
     *
     * @return this
     */
    public Builder yellow() {
      return color(Color.YELLOW);
    }

    /**
     * 设置前景色为蓝色。常用于信息性文本。
     *
     * @return this
     */
    public Builder blue() {
      return color(Color.BLUE);
    }

    /**
     * 设置前景色为洋红色。
     *
     * @return this
     */
    public Builder magenta() {
      return color(Color.MAGENTA);
    }

    /**
     * 设置前景色为青色。常用于标题、强调信息。
     *
     * @return this
     */
    public Builder cyan() {
      return color(Color.CYAN);
    }

    /**
     * 设置前景色为白色。
     *
     * @return this
     */
    public Builder white() {
      return color(Color.WHITE);
    }

    /**
     * 启用加粗样式。可与颜色和其他修饰组合使用。
     *
     * @return this
     */
    public Builder bold() {
      options.add(StyleOption.BOLD);
      return this;
    }

    /**
     * 启用下划线样式。可与颜色和其他修饰组合使用。
     *
     * @return this
     */
    public Builder underline() {
      options.add(StyleOption.UNDERLINE);
      return this;
    }

    /**
     * 启用斜体样式。可与颜色和其他修饰组合使用。部分终端可能不支持斜体渲染。
     *
     * @return this
     */
    public Builder italic() {
      options.add(StyleOption.ITALIC);
      return this;
    }

    /**
     * 启用删除线样式。可与颜色和其他修饰组合使用。部分终端可能不支持删除线渲染。
     *
     * @return this
     */
    public Builder strikethrough() {
      options.add(StyleOption.STRIKETHROUGH);
      return this;
    }

    /**
     * 重置为默认样式（无颜色、无修饰）。
     *
     * <p>调用后，后续 {@link #append(String)} 将使用终端默认颜色和样式，直到再次设置颜色或修饰。
     *
     * @return this
     */
    public Builder normal() {
      color = null;
      options.clear();
      return this;
    }

    /**
     * 以当前样式追加文本段。
     *
     * <p>将当前颜色、修饰选项的快照与文本绑定，创建一个新的 {@link Segment} 并添加到内部列表中。 后续修改样式不会影响已追加的段。
     *
     * @param text 要追加的文本（不能为 {@code null}）
     * @return this
     */
    public Builder append(final String text) {
      segments.add(new Segment(text, color, options));
      return this;
    }

    /**
     * 构建不可变的 {@link StyledText} 实例。
     *
     * <p>构建完成后，返回的 {@code StyledText} 对象完全不可变，可在多线程环境中安全共享。 此 Builder 实例可继续使用以创建新的 {@code
     * StyledText}（但通常建议为每个对象创建新的 Builder）。
     *
     * @return 构建完成的 StyledText 实例
     */
    public StyledText build() {
      return new StyledText(segments);
    }
  }
}
