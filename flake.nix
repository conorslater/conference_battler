# ./flake.nix
#
# Development environment for Conference Battler, a Barcode Battler-style
# Android game (Kotlin + Jetpack Compose + CameraX + ML Kit).
#
# Unlike ../android/flake.nix (LineageOS ROM builds), this is a plain mkShell,
# not a buildFHSEnv. An app build needs only a JDK, Gradle and an Android SDK,
# and nixpkgs' androidenv ships an SDK whose binaries are already patched to
# run on NixOS. The ROM flake needs FHS because it runs the AOSP tree's own
# dynamically linked prebuilts.
#
# adb is not provided here: the host already gets android-tools and the ADB
# udev rule from ../nixos/packages/adb.nix.
#
# Gradle itself comes from ./gradlew (wrapper pinned to 8.13, same as
# ../DevilutionX/android-project). The wrapper distribution is pure JVM, so it
# runs fine on NixOS. It downloads Maven dependencies into ~/.gradle on first
# build.
{
  description = "Conference Battler: Android build environment (JDK 17, Android SDK 35)";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-26.05";
  };

  outputs = { self, nixpkgs }:
    let
      # x86_64 only: androidenv's prebuilt SDK packages (aapt2, d8, ...) are
      # x86_64 Linux binaries.
      systems = [ "x86_64-linux" ];

      # Explicit rather than flake-utils, as in ../alfresco/flake.nix: one
      # input fewer, and flake-utils is in maintenance mode.
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f (import nixpkgs {
        inherit system;
        config = {
          # The Android SDK is unfree and requires accepting its licence.
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      }));

      # Must match compileSdk / buildToolsVersion in app/build.gradle.kts. If
      # they differ, AGP tries to download the missing parts into the SDK,
      # which is a read-only Nix store path, and the build fails.
      platformVersion = "35";
      buildToolsVersion = "35.0.0";

      mkAndroidSdk = pkgs: (pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ platformVersion ];
        buildToolsVersions = [ buildToolsVersion ];
        includeEmulator = false;
        includeSystemImages = false;
        includeNDK = false; # ML Kit and CameraX are plain Maven AARs, no NDK needed.
      }).androidsdk;
    in
    {
      devShells = forAllSystems (pkgs:
        let
          androidSdk = mkAndroidSdk pkgs;
          sdkRoot = "${androidSdk}/libexec/android-sdk";
        in
        {
          default = pkgs.mkShell {
            packages = [
              androidSdk
              pkgs.jdk17 # AGP 8.13 requires JDK 17+.
              pkgs.kotlin-language-server
            ];

            ANDROID_HOME = sdkRoot;
            ANDROID_SDK_ROOT = sdkRoot;
            JAVA_HOME = pkgs.jdk17.home;

            # AGP normally downloads aapt2 from Google's Maven repo. That aapt2
            # is a dynamically linked generic-Linux binary that can't run on
            # NixOS ("No such file or directory" from the missing loader).
            # Point AGP at the SDK's patched aapt2 instead.
            GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkRoot}/build-tools/${buildToolsVersion}/aapt2";

            shellHook = ''
              export CB_TOP="$PWD"
              export CB_LOG_DIR="$CB_TOP/Logs"
              mkdir -p "$CB_LOG_DIR"

              # Run any gradle task, teeing EVERYTHING to Logs/build.log
              # (overwritten each run). pipefail must be set inside the
              # subshell, or the function returns tee's 0 on a failed build.
              cb-build() {
                ( set -o pipefail
                  cd "$CB_TOP" && ./gradlew "$@" 2>&1 | tee "$CB_LOG_DIR/build.log"
                )
              }

              echo ""
              echo "Conference Battler build environment"
              echo "  $(java -version 2>&1 | head -n1)"
              echo "  ANDROID_HOME=$ANDROID_HOME"
              echo ""
              echo "  cb-build test               # JVM unit tests      -> Logs/build.log"
              echo "  cb-build assembleDebug      # debug APK           -> Logs/build.log"
              echo "  cb-build installDebug       # install via adb     -> Logs/build.log"
              echo ""
            '';
          };
        });

      formatter = forAllSystems (pkgs: pkgs.nixfmt-rfc-style);
    };
}
