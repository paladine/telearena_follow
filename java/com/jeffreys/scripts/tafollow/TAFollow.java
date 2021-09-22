package com.jeffreys.scripts.tafollow;

import com.google.common.collect.ImmutableSet;
import com.jeffreys.common.ansi.AnsiColorParser;
import com.jeffreys.common.ansi.AnsiColorParser.ParsedAnsiText;
import com.jeffreys.common.queue.NonBlockingSupplier;
import com.jeffreys.scripts.common.Triggers;
import java.io.PrintWriter;
import java.time.Duration;
import javax.inject.Inject;

public class TAFollow {
  private final NonBlockingSupplier<String> lineSupplier;
  private final PrintWriter output;
  private final PrintWriter logfile;
  private final Configuration configuration;
  private final AnsiColorParser ansiColorParser = new AnsiColorParser();
  private final ImmutableSet<String> expectedCommands;
  private final Triggers triggers;
  private final Duration lineGetTimeoutDuration;

  @Inject
  TAFollow(
      NonBlockingSupplier<String> lineSupplier,
      @Annotations.OutputPrintWriter PrintWriter output,
      @Annotations.LogfilePrintWriter PrintWriter logfile,
      Configuration configuration,
      Triggers triggers) {
    this.lineSupplier = lineSupplier;
    this.output = output;
    this.logfile = logfile;
    this.configuration = configuration;
    this.triggers = triggers;
    this.lineGetTimeoutDuration = Duration.ofMillis(configuration.getIdleCommandWaitMilliseconds());

    ImmutableSet.Builder<String> expectedCommandsBuilder = ImmutableSet.builder();
    for (String owner : configuration.getOwnerList()) {
      expectedCommandsBuilder.add(String.format("From %s (whispered): ", owner));
      expectedCommandsBuilder.add(String.format("From %s (to group): ", owner));
    }
    this.expectedCommands = expectedCommandsBuilder.build();
  }

  private void attack(String target) {
    String command = String.format("a %s\r\n", target);
    for (int i = 0; i < configuration.getNumberOfPhysicalAttacks(); ++i) {
      output.print(command);
    }
    output.flush();
  }

  private void attackWithSpell(String target) {
    attack(target);

    if (!configuration.getAttackSpell().isEmpty()) {
      output.printf("c %s %s\r\n", configuration.getAttackSpell(), target);
    }
  }

  private void attackWithGroupSpell(String target) {
    attack(target);

    if (!configuration.getGroupAttackSpell().isEmpty()) {
      output.printf("c %s\r\n", configuration.getGroupAttackSpell());
    }
  }

  private void heal(String target) {
    if (!configuration.getHealSpell().isEmpty()) {
      output.printf("c %s %s\r\n", configuration.getHealSpell(), target);
    }
  }

  private void healGroup() {
    if (!configuration.getGroupHealSpell().isEmpty()) {
      output.printf("c %s\r\n", configuration.getGroupHealSpell());
    }
  }

  private void doLiteralCommand(String action) {
    if (action.equalsIgnoreCase("suicide")) {
      return;
    }

    output.printf("%s\r\n", action);
  }

  private void parseAction(String action) {
    action = action.trim();

    if (action.startsWith("a ")) {
      attack(action.substring(2));
    } else if (action.startsWith("as ")) {
      attackWithSpell(action.substring(3));
    } else if (action.startsWith("ag ")) {
      attackWithGroupSpell(action.substring(3));
    } else if (action.startsWith("heal ")) {
      heal(action.substring(5));
    } else if (action.startsWith("healg")) {
      healGroup();
    } else {
      doLiteralCommand(action);
    }
  }

  private void parseLine(ParsedAnsiText line) {
    int index;

    triggers.processLine(line, (id, command) -> output.printf("%s\r\n", command));

    for (String expectedCommand : expectedCommands) {
      index = line.getText().indexOf(expectedCommand);
      if (index >= 0) {
        parseAction(line.getText().substring(index + expectedCommand.length()));
        return;
      }
    }
  }

  void run() throws Exception {
    try {
      while (true) {
        String line = lineSupplier.get(lineGetTimeoutDuration);
        if (line == null) {
          if (!configuration.getIdleCommand().isEmpty()) {
            output.print(configuration.getIdleCommand());
            output.flush();
          }
          continue;
        }

        logfile.println(line);
        logfile.flush();

        parseLine(ansiColorParser.parseAnsi(line));
      }
    } catch (Throwable t) {
      if (!configuration.getLogOffCommand().isEmpty()) {
        output.printf("%s\r\n", configuration.getLogOffCommand());
        output.flush();
      }
      throw t;
    }
  }
}
