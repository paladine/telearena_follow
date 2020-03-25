package(default_visibility = [
    "//javatests/com/jeffreys/scripts/tafollow:__subpackages__"
])

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
    srcs = glob(["*.java"]),
    deps = [
        ":java_follow_proto",
        "//java/com/jeffreys/common/ansi",
        "//java/com/jeffreys/common/proto",
        "//java/com/jeffreys/scripts/common",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)

java_binary(
    name = "TAFollow",
    main_class = "com.jeffreys.scripts.tafollow.TAFollow",
    runtime_deps = [":lib"],
)
