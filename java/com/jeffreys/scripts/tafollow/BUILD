package(default_visibility = [
    "//javatests/com/jeffreys/scripts/tafollow:__subpackages__"
])

BINARY_SRCS = ["Main.java", "Options.java"]

proto_library(
    name = "follow_proto",
    srcs = ["follow.proto"],
    deps = ["//java/com/jeffreys/scripts/common:common_proto"],
)

java_proto_library(
    name = "java_follow_proto",
    deps = [":follow_proto"],
)

java_library(
    name = "lib",
    srcs = glob(["*.java"], exclude = BINARY_SRCS),
    deps = [
        ":java_follow_proto",
        "//java/com/jeffreys/common/ansi",
        "//java/com/jeffreys/common/proto",
        "//java/com/jeffreys/common/queue",
        "//java/com/jeffreys/scripts/common",
        "//third_party/com/google/auto/value",
        "@org_kohsuke_arg4j//:args4j",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:javax_inject_javax_inject",
    ],
)

java_binary(
    name = "TAFollow",
    srcs = BINARY_SRCS,
    main_class = "com.jeffreys.scripts.tafollow.Main",
    deps = [
        ":java_follow_proto",
        ":lib",
        "//java/com/jeffreys/common/proto",
        "//java/com/jeffreys/common/queue",
        "//java/com/jeffreys/scripts/common",
        "//third_party/com/google/auto/value",
        "@org_kohsuke_arg4j//:args4j",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
    ],
)
