/*
  Copyright (c) 2026, YashanDB Development Group.
*/

package com.yashandb.streamai.console.jline;

import com.yashandb.streamai.console.IConsole;
import com.yashandb.streamai.console.StyledText;
import com.yashandb.streamai.exception.SaiClosedException;
import com.yashandb.streamai.exception.SaiStartFailedException;
import com.yashandb.streamai.exception.SaiUnexpectedException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * 基于 JLine 的控制台实现，通过 {@code printAbove} 实现读写无锁并发。
 *
 * <h2>设计说明</h2>
 *
 * <p>{@code CliConsole} 是 {@link IConsole} 接口的 JLine 实现。它封装了 JLine 的 {@code Terminal} 和 {@code
 * LineReader}， 将 {@link StyledText} 转换为 JLine 的 {@code AttributedString} 输出，并将 {@link
 * CompletionProvider} 桥接为 JLine 的 {@code Completer}。
 *
 * <h2>核心特性</h2>
 *
 * <ul>
 *   <li><b>并发安全</b>：通过 {@code lineReader.printAbove()} 实现输出插入到当前输入提示行上方，不干扰用户正在输入的提示行
 *   <li><b>富文本支持</b>：将 {@link StyledText} 的颜色、加粗、下划线、斜体、删除线映射为 JLine 的 {@code AttributedStyle}
 *   <li><b>代码补全</b>：将 {@link CompletionProvider} 桥接为 JLine 的 {@code Completer}，支持 Tab 键自动补全
 *   <li><b>粘贴支持</b>：启用 {@code BRACKETED_PASTE} 选项，支持多行粘贴
 * </ul>
 *
 * <h2>使用方式</h2>
 *
 * <p>推荐通过 {@link ConsoleFactory} 创建实例，而非直接调用构造函数：
 *
 * <pre>{@code
 * // 无补全
 * try (IConsole console = ConsoleFactory.create()) {
 *     console.print("Hello");
 * }
 *
 * // 带补全
 * CommandCompleter completer = new CommandCompleter("help", "quit");
 * try (IConsole console = ConsoleFactory.create(completer)) {
 *     console.print("Hello");
 * }
 * }</pre>
 *
 * <h2>线程安全</h2>
 *
 * <p>{@code closed} 状态使用 {@code volatile} 保证可见性。{@code readLine()} 是阻塞操作， 而 {@code print()}
 * 可在任意线程调用，通过 {@code printAbove} 保证并发安全。
 *
 * @see IConsole
 * @see ConsoleFactory
 * @see StyledText
 */
@Slf4j
public final class CliConsole implements IConsole {
  private static final String PROMPT = "stream-ai> ";
  private final Terminal terminal;
  private final LineReader lineReader;
  private volatile boolean closed;

  /**
   * 构造 CliConsole，初始化终端，并注册代码补全提供者。
   *
   * @param completionProvider 补全提供者（不能为 {@code null}）
   * @throws SaiStartFailedException 终端初始化失败时抛出
   */
  public CliConsole(final CompletionProvider completionProvider) throws SaiStartFailedException {
    try {
      this.terminal = TerminalBuilder.builder().system(true).build();
      final LineReaderBuilder builder = LineReaderBuilder.builder().terminal(terminal);
      if (completionProvider != null) {
        builder.completer(toJlineCompleter(completionProvider));
      }
      this.lineReader = builder.build();
      lineReader.setOpt(LineReader.Option.BRACKETED_PASTE);
      this.closed = false;
    } catch (final IOException e) {
      throw new SaiStartFailedException("Failed to initialize terminal", e);
    }
  }

  /**
   * 测试用构造函数，接受外部注入的 Terminal 和 LineReader。
   *
   * @param terminal JLine 终端实例
   * @param lineReader JLine 行读取器实例
   */
  public CliConsole(final Terminal terminal, final LineReader lineReader) {
    this.terminal = terminal;
    this.lineReader = lineReader;
    this.closed = false;
  }

  @Override
  public String readLine() throws SaiClosedException {
    checkClosed();
    try {
      return lineReader.readLine(PROMPT);
    } catch (final UserInterruptException e) {
      return "";
    } catch (final EndOfFileException e) {
      return null;
    } catch (final Exception e) {
      throw new SaiUnexpectedException("readLine failed", e);
    }
  }

  @Override
  public void print(final String text) throws SaiClosedException {
    print(StyledText.of(text));
  }

  @Override
  public void print(final StyledText text) throws SaiClosedException {
    checkClosed();
    final AttributedStringBuilder asb = new AttributedStringBuilder();
    for (final StyledText.Segment seg : text.getSegments()) {
      AttributedStyle style = AttributedStyle.DEFAULT;
      if (seg.getOptions().contains(StyledText.StyleOption.BOLD)) {
        style = style.bold();
      }
      if (seg.getOptions().contains(StyledText.StyleOption.UNDERLINE)) {
        style = style.underline();
      }
      if (seg.getOptions().contains(StyledText.StyleOption.ITALIC)) {
        style = style.italic();
      }
      if (seg.getOptions().contains(StyledText.StyleOption.STRIKETHROUGH)) {
        style = style.crossedOut();
      }
      if (seg.getColor() != null) {
        style = style.foreground(toJlineColor(seg.getColor()));
      }
      asb.style(style).append(seg.getText());
    }
    lineReader.printAbove(asb.toAttributedString());
  }

  private static int toJlineColor(final StyledText.Color color) {
    return switch (color) {
      case BLACK -> AttributedStyle.BLACK;
      case RED -> AttributedStyle.RED;
      case GREEN -> AttributedStyle.GREEN;
      case YELLOW -> AttributedStyle.YELLOW;
      case BLUE -> AttributedStyle.BLUE;
      case MAGENTA -> AttributedStyle.MAGENTA;
      case CYAN -> AttributedStyle.CYAN;
      case WHITE -> AttributedStyle.WHITE;
      default -> throw new SaiUnexpectedException("Unknown color: " + color);
    };
  }

  /**
   * 将 CompletionProvider 桥接为 JLine Completer。
   *
   * @param provider 补全提供者
   * @return JLine Completer 实例
   */
  private static Completer toJlineCompleter(final CompletionProvider provider) {
    return (reader, line, candidates) -> {
      final List<CompletionCandidate> result = provider.complete(line.line(), line.cursor());
      for (final CompletionCandidate c : result) {
        candidates.add(
            new Candidate(c.getValue(), c.getValue(), null, c.getDescription(), null, null, true));
      }
    };
  }

  @Override
  public void copy(final String text) throws SaiClosedException {
    checkClosed();
    try {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(text), null);
    } catch (final Exception e) {
      throw new SaiUnexpectedException("copy to clipboard failed", e);
    }
  }

  private void checkClosed() throws SaiClosedException {
    if (closed) {
      throw new SaiClosedException("Console is closed");
    }
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    try {
      terminal.close();
      LOG.info("Console closed");
    } catch (final IOException e) {
      LOG.warn("Failed to close terminal: {}", e.getMessage());
    }
  }
}
