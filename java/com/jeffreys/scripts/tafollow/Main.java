package com.jeffreys.scripts.tafollow;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.jeffreys.common.proto.Protos;
import com.jeffreys.common.queue.NonBlockingSupplier;
import com.jeffreys.common.queue.NonBlockingSuppliers;
import com.jeffreys.scripts.common.Triggers;
import com.jeffreys.scripts.tafollow.Annotations.LogfilePrintWriter;
import com.jeffreys.scripts.tafollow.Annotations.OutputPrintWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  private static class ConfigurationProtoModule extends AbstractModule {
    @Override
    public void configure() {
      bind(Clock.class).toInstance(Clock.systemUTC());
      bind(Scanner.class).toInstance(new Scanner(System.in));
      bind(PrintWriter.class)
          .annotatedWith(OutputPrintWriter.class)
          .toInstance(new PrintWriter(System.out, /* autoFlush= */ true));
    }

    @Provides
    Configuration provideConfiguration(Options options) {
      return Protos.parseProtoFromTextFile(options.getConfigFile(), Configuration.class);
    }

    @Provides
    @LogfilePrintWriter
    PrintWriter provideLogfilePrintWriter(Configuration configuration)
        throws FileNotFoundException {
      OutputStream outputStream =
          configuration.getLogFile().isEmpty()
              ? ByteStreams.nullOutputStream()
              : new FileOutputStream(configuration.getLogFile());

      return new PrintWriter(outputStream, /* autoFlush= */ true);
    }

    @Provides
    Triggers provideTriggers(Configuration configuration) {
      return Triggers.of(configuration.getTriggersList());
    }

    @Provides
    @Singleton
    ExecutorService provideExecutor() {
      return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    NonBlockingSupplier<String> provideNonBlockingSupplier(
        Scanner scanner, ExecutorService executor) {
      return NonBlockingSuppliers.createNonBlockingSupplier(
          /* capacity= */ 256, executor, scanner::nextLine);
    }
  }

  public static void main(String[] args) {
    ExecutorService executor = null;
    try {
      Injector injector =
          Guice.createInjector(new ConfigurationProtoModule(), Options.getModule(args));
      TAFollow taFollow = injector.getInstance(TAFollow.class);
      executor = injector.getInstance(ExecutorService.class);

      taFollow.run();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    } finally {
      if (executor != null) {
        MoreExecutors.shutdownAndAwaitTermination(executor, Duration.ofSeconds(3));
      }
    }
  }
}
