package com.jeffreys.scripts.tafollow;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.jeffreys.common.ansi.AnsiColorParser;
import com.jeffreys.common.ansi.AnsiColorParser.ParsedAnsiText;
import com.jeffreys.common.proto.Protos;
import com.jeffreys.scripts.common.Triggers;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class TAFollow {
  private static final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

  private final Scanner scanner;
  private final PrintWriter output;
  private final PrintWriter logfile;
  private final Configuration configuration;
  private final AnsiColorParser ansiColorParser = new AnsiColorParser();
  private final ImmutableSet<String> expectedCommands;
  private final Triggers triggers;

  TAFollow(Scanner scanner, PrintWriter output, PrintWriter logfile, Configuration configuration) {
    this.scanner = scanner;
    this.output = output;
    this.logfile = logfile;
    this.configuration = configuration;
    this.triggers = Triggers.of(configuration.getTriggersList());

    ImmutableSet.Builder<String> expectedCommandsBuilder = ImmutableSet.builder();
    for (String owner : configuration.getOwnerList()) {
      expectedCommandsBuilder.add(String.format("From %s (whispered): ", owner));
      expectedCommandsBuilder.add(String.format("From %s (to group): ", owner));
    }
    expectedCommands = expectedCommandsBuilder.build();
  }

  private static PrintWriter getPrintWriter(String file) throws IOException {
    OutputStream outputStream =
        file.isEmpty() ? ByteStreams.nullOutputStream() : new FileOutputStream(file);
    return new PrintWriter(outputStream, /* autoFlush= */ true);
  }

  public static void main(String[] args) {
    try {
      Configuration configuration = Protos.parseProtoFromTextFile(args[0], Configuration.class);

      new TAFollow(
              new Scanner(System.in),
              new PrintWriter(System.out, /* autoflush= */ true),
              getPrintWriter(configuration.getLogFile()),
              configuration)
          .run();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
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

  void run() {
    try {
      while (true) {
        String line = scanner.nextLine();

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
