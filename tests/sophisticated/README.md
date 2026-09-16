# Sophisticated water purity integration tests

Tested with Minecraft 1.21.1, NeoForge 21.1.215, Sophisticated Core 1.3.89.1239,
and Sophisticated Backpacks 3.25.14.1410. Put the two mod JARs in `build/run/mods`.

Run from the repository root:

```powershell
.\gradlew.bat -I tests/sophisticated/init.gradle runGameTestServer --no-configuration-cache --console=plain
```

Require `All 2 required tests passed` in the server log. A successful Gradle exit alone
is insufficient: the development launcher can return success for mod loading failures.

Coverage: purity 0–3 through bottle filling and draining, request-versus-stored purity,
simulation, insufficient water, rejected lava, experience bottles, disabled purity,
actual backpack tank mixing in both orders, zero-volume fill, serialization/reload,
and tank-to-bottle transfer. Tests call the actual transformed Core handlers.

The test init script excludes the old development Create dependency at runtime and
limits the test JVM's compiler tier to avoid a Zulu 21.0.4 native compiler crash on
the test machine. Neither setting affects normal builds. Test sources and structure
are opt-in and must not be shipped; run normal `compileJava processResources` afterward.

Latest upstream source reviewed: Sophisticated Core 1.21.x at
`aadb8a2483ace15a5b62d9f793c8523834615b42`. Core 1.5.1.2341 runtime verification
was blocked by the newer NeoForge dependency download; it is not claimed tested.
