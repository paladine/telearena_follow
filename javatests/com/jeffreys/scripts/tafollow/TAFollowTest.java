package com.jeffreys.scripts.tafollow;

import static com.google.common.truth.Truth.assertThat;
import static com.jeffreys.junit.Exceptions.assertThrows;

import com.jeffreys.scripts.common.Color;
import com.jeffreys.scripts.common.Trigger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TAFollowTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintWriter output = new PrintWriter(outputStream, /* autoFlush= */ true);

  private final ByteArrayOutputStream logFileOutputStream = new ByteArrayOutputStream();
  private final PrintWriter logOutput = new PrintWriter(logFileOutputStream, /* autoFlush= */ true);

  private static final String RED = "\u001B[1;31m";
  private static final String GREEN = "\u001B[1;32m";
  private static final String YELLOW = "\u001B[1;33m";
  private static final String BLUE = "\u001B[1;34m";
  private static final String MAGENTA = "\u001B[1;35m";
  private static final String CYAN = "\u001B[1;36m";
  private static final String WHITE = "\u001B[1;37m";

  @Test
  public void emptyRun() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream = new ByteArrayInputStream("Hahaha no\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void emptyRun_doesLogOffCommand() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setLogOffCommand("=x\r\n")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream = new ByteArrayInputStream("Hahaha no\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("=x\r\n\r\n");
  }

  @Test
  public void attacks_whispered() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (whispered): a st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_toGroup() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): a st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_withSpell() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setAttackSpell("tamikar")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): as st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\nc tamikar st\r\n");
  }

  @Test
  public void attacks_withSpell_noSpellConfigured() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): as st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_withGroupSpell() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setGroupAttackSpell("dakidaku")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): ag st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\nc dakidaku\r\n");
  }

  @Test
  public void attacks_withGroupSpell_noSpellConfigured() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): ag st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void heals() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setHealSpell("kusamotu")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): heal st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("c kusamotu st\r\n");
  }

  @Test
  public void heals_noSpellConfigured() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): heal st\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void healsGroup() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setGroupHealSpell("kusamotumaru")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): healg\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("c kusamotumaru\r\n");
  }

  @Test
  public void healsGroup_noSpellConfigured() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): healg\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_command() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): kiss me\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("kiss me\r\n");
  }

  @Test
  public void literal_command_with_extra_space() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): health    \r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("health\r\n");
  }

  @Test
  public void literal_command_suicide() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): suicide\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_command_suicide_with_extra_space() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Super Conductor (to group): suicide     \r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_multipleOwners() {
    Configuration configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .addOwner("Paladine")
            .setNumberOfPhysicalAttacks(2)
            .build();

    ByteArrayInputStream inputStream =
        new ByteArrayInputStream("From Paladine (to group): kiss me\r\n".getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("kiss me\r\n");
  }

  @Test
  public void shareMoney_shares() {
    Configuration configuration =
        Configuration.newBuilder()
            .addTriggers(
                Trigger.newBuilder()
                    .setExpectedColor(Color.CYAN)
                    .setTriggerRegex(".*You found (\\d+) gold crowns while searching the area\\..*")
                    .setCommand("sh $1")
                    .build())
            .build();

    String text = CYAN + "You found 200 gold crowns while searching the area.\r\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEqualTo("sh 200\r\n");
  }

  @Test
  public void shareMoney_noSharingEnabled() {
    Configuration configuration = Configuration.getDefaultInstance();

    String text = CYAN + "You found 200 gold crowns while searching the area.\r\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void logfile_writes() {
    Configuration configuration = Configuration.getDefaultInstance();

    String text = CYAN + "You found 200 gold crowns while searching the area.\r\nMore stuff\r\n";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());

    execute(new TAFollow(new Scanner(inputStream), output, logOutput, configuration));

    assertThat(logFileOutputStream.toString()).isEqualTo(text.replaceAll("\r", ""));
  }

  private static void execute(TAFollow script) {
    assertThrows(NoSuchElementException.class, () -> script.run());
  }
}
