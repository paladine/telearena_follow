package com.jeffreys.scripts.tafollow;

import static com.google.common.truth.Truth.assertThat;
import static com.jeffreys.junit.Exceptions.assertThrows;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.jeffreys.common.queue.NonBlockingSupplier;
import com.jeffreys.scripts.common.Color;
import com.jeffreys.scripts.common.Trigger;
import com.jeffreys.scripts.common.Triggers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.inject.Inject;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TAFollowTest {

  private static final String RED = "\u001B[1;31m";
  private static final String GREEN = "\u001B[1;32m";
  private static final String YELLOW = "\u001B[1;33m";
  private static final String BLUE = "\u001B[1;34m";
  private static final String MAGENTA = "\u001B[1;35m";
  private static final String CYAN = "\u001B[1;36m";
  private static final String WHITE = "\u001B[1;37m";

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  @Bind @Annotations.OutputPrintWriter
  private final PrintWriter output = new PrintWriter(outputStream, /* autoFlush= */ true);

  private final ByteArrayOutputStream logFileOutputStream = new ByteArrayOutputStream();

  @Bind @Annotations.LogfilePrintWriter
  private final PrintWriter logOutput = new PrintWriter(logFileOutputStream, /* autoFlush= */ true);

  @Bind(lazy = true)
  private Configuration configuration =
      Configuration.newBuilder().setNumberOfPhysicalAttacks(2).build();

  @Bind(lazy = true)
  private NonBlockingSupplier<String> lineSupplier;

  @Inject private Provider<TAFollow> tafollow;

  @Before
  public void injectMembers() {
    Guice.createInjector(
            BoundFieldModule.of(this),
            new AbstractModule() {
              @Provides
              Triggers provideTriggers(Configuration configuration) {
                return Triggers.of(configuration.getTriggersList());
              }
            })
        .injectMembers(this);
  }

  @Test
  public void emptyRun() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("Hahaha no\r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void emptyRun_doesLogOffCommand() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setLogOffCommand("=x\r\n")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("Hahaha no\r\n");

    assertThat(outputStream.toString()).isEqualTo("=x\r\n\r\n");
  }

  @Test
  public void attacks_whispered() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (whispered): a st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_toGroup() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): a st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_withSpell() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setAttackSpell("tamikar")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): as st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\nc tamikar st\r\n");
  }

  @Test
  public void attacks_withSpell_noSpellConfigured() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): as st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void attacks_withGroupSpell() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setGroupAttackSpell("dakidaku")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): ag st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\nc dakidaku\r\n");
  }

  @Test
  public void attacks_withGroupSpell_noSpellConfigured() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): ag st\r\n");

    assertThat(outputStream.toString()).isEqualTo("a st\r\na st\r\n");
  }

  @Test
  public void heals() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setHealSpell("kusamotu")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): heal st\r\n");

    assertThat(outputStream.toString()).isEqualTo("c kusamotu st\r\n");
  }

  @Test
  public void heals_noSpellConfigured() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): heal st\r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void healsGroup() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setGroupHealSpell("kusamotumaru")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): healg\r\n");

    assertThat(outputStream.toString()).isEqualTo("c kusamotumaru\r\n");
  }

  @Test
  public void healsGroup_noSpellConfigured() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): healg\r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_command() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): kiss me\r\n");

    assertThat(outputStream.toString()).isEqualTo("kiss me\r\n");
  }

  @Test
  public void literal_command_with_extra_space() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): health    \r\n");

    assertThat(outputStream.toString()).isEqualTo("health\r\n");
  }

  @Test
  public void literal_command_suicide() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): suicide\r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_command_suicide_with_extra_space() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Super Conductor (to group): suicide     \r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void literal_multipleOwners() {
    configuration =
        Configuration.newBuilder()
            .addOwner("Super Conductor")
            .addOwner("Paladine")
            .setNumberOfPhysicalAttacks(2)
            .build();

    execute("From Paladine (to group): kiss me\r\n");

    assertThat(outputStream.toString()).isEqualTo("kiss me\r\n");
  }

  @Test
  public void shareMoney_shares() {
    configuration =
        Configuration.newBuilder()
            .addTriggers(
                Trigger.newBuilder()
                    .setExpectedColor(Color.CYAN)
                    .setTriggerRegex(".*You found (\\d+) gold crowns while searching the area\\..*")
                    .setCommand("sh $1")
                    .build())
            .build();

    execute(CYAN + "You found 200 gold crowns while searching the area.\r\n");

    assertThat(outputStream.toString()).isEqualTo("sh 200\r\n");
  }

  @Test
  public void shareMoney_noSharingEnabled() {
    configuration = Configuration.getDefaultInstance();

    execute(CYAN + "You found 200 gold crowns while searching the area.\r\n");

    assertThat(outputStream.toString()).isEmpty();
  }

  @Test
  public void logfile_writes() {
    configuration = Configuration.getDefaultInstance();

    String text = CYAN + "You found 200 gold crowns while searching the area.\r\nMore stuff\r\n";
    execute(text);

    assertThat(logFileOutputStream.toString()).isEqualTo(text.replaceAll("\r", ""));
  }

  @Test
  public void idle_writes() {
    configuration =
        configuration.toBuilder()
            .setIdleCommand("idle\r\nyeah\r\n")
            .setIdleCommandWaitMilliseconds(5000)
            .build();

    // does 2 nulls, then throws out of input
    lineSupplier =
        new NonBlockingSupplier<String>() {
          private int callCount = 0;

          @Override
          public String get(Duration duration) {
            ++callCount;
            if (callCount > 2) {
              throw new NoSuchElementException("No more data");
            }
            return null;
          }
        };

    assertThrows(NoSuchElementException.class, () -> tafollow.get().run());

    assertThat(outputStream.toString()).isEqualTo("idle\r\nyeah\r\nidle\r\nyeah\r\n");
  }

  private void execute(String input) {
    Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(UTF_8)));

    lineSupplier = timeout -> scanner.nextLine();

    assertThrows(NoSuchElementException.class, () -> tafollow.get().run());
  }
}
