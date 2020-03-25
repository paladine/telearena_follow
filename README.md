# telearena_follow
Tele-Arena Follow Script. For automating the playing of more than one character at a time.

## How to build?
Install [bazel](https://bazel.build/)

To build a deployable jar, run `bazel build //java/com/jeffreys/scripts/tafollow:TAFollow_deploy.jar`

## How to test?
To execute tests, run `bazel test ...`

## How to execute?
`java -jar TAFollow.jar <file with text proto of scripts.tafollow.Configuration>`
  
You can also run out of the repo directory, `bazel run //java/com/jeffreys/scripts/tafollow:TAFollow -- <file with text proto>`

But you're connected to a BBS playing TA, so you need to execute this in the context of my other program - [TelnetScripter](https://github.com/paladine/telnet_scripter)

## How do you run this script?
Use [TelnetScripter](https://github.com/paladine/telnet_scripter), connect to your system, and then execute the script by 
writing a wrapper script

```
#!/bin/sh

java -jar TAFollow.jar configuration.textproto
```

## How to stop your script?
You can kill the script process in your OS. You cannot stop it via special text commands.

## Why Java?
For maximum cross platform support. You can run this on Windows, Linux, Raspberry Pi, etc.

## Features
Once the script is running, either whisper or group chat to your followers to signal a command. There are 
several built-in commands which I'll label below. Anything not a command will just be sent verbatim, e.g. if you
whisper `/character do this`, they'll execute `do this`

  * Attacking
    * Define `number_of_physical_attacks`, command `a <target>`
  * Attacking with a single spell
    * Define `attack_spell`, command `as <target>`
  * Attacking with an area spell
    * Define `group_spell`, command `ag <target>`
  * Healing a target
    * Define `heal_spell`, command `heal <target>`
  * Healing your group
    * Define `group_heal_spell`, command `healg`
  * Logging off
    * In the case of script failure, rather than just exiting quietly, you can define `logoff_command` to
      execute
  * Logging data
    * Define `log_file` and all script output will be written to this file for later analysis/debugging
  * Triggers
    * A powerful regex trigger based system let's you match inputs and autogenerate outputs. See the example
      below to see how to use this feature
      
## Sample requirements script
See the [text proto definition](https://github.com/paladine/telearena_follow/blob/master/java/com/jeffreys/scripts/tafollow/follow.proto)
for the list of available fields.

Here's an example:
```
owner: "Par-Salian"
owner: "Paladine"
owner: "Fistandantilus"
owner: "Justarius"
number_of_physical_attacks: 6
log_file: "/tmp/tasslehoff.log"
triggers: {
  expected_color: CYAN
  trigger_regex: ".*You found (\\d+) gold crowns while searching the.*"
  command: "sh $1"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*Mana:         (\\d+) \\/ (\\d+).*"
  command: "Mana: [$1 / $2]"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*Vitality:     (\\d+) \\/ (\\d+).*"
  command: "Vitality: [$1 / $2]"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*Status:       (.*).*"
  command: "Status: [$1]"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*Level:        (\\d+).*"
  command: "Level: [$1]"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*Experience:   (\\d+).*"
  command: "Exp: [$1]"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*You are no longer a member of .* group.*"
  command: "=x\r\n"
}
triggers: {
  expected_color: ANY
  trigger_regex: ".*has just disbanded the group!.*"
  command: "=x\r\n"
}
```
