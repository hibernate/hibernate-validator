# Hibernate Validator Benchmark Runner

Wrapper script for running JMH benchmarks in the `performance` module with support for async-profiler flamegraphs,
JMH built-in profilers, and CPU core pinning.

## Prerequisites

- For **Java**/**Maven** look at [CONTRIBUTING.md](../../CONTRIBUTING.md)
- **Linux** or **macOS**
- For async-profiler on Linux: `perf_event_paranoid <= 1` (see [Async-profiler](#async-profiler--flamegraphs))

## Quick Start

```bash
# Run all default benchmarks with hv-current
./performance/scripts/run-benchmarks.sh

# Quick dev iteration run
./performance/scripts/run-benchmarks.sh --quick

# Run a specific benchmark
./performance/scripts/run-benchmarks.sh --quick CascadedValidation

# Generate a CPU flamegraph
./performance/scripts/run-benchmarks.sh --quick --async-profiler SimpleValidation
```

## Usage

```
./performance/scripts/run-benchmarks.sh [OPTIONS] [BENCHMARK_PATTERNS...]
```

Benchmark patterns are JMH regex patterns matched against fully qualified benchmark method names.
Multiple patterns can be specified. If none are given, the default benchmark set is run.

## Options

| Option | Default | Description |
|--------|---------|-------------|
| `--profile <name>` | `hv-current` | Validator profile to build and run |
| `--skip-build` | | Skip Maven build, use existing JAR |
| `--forks <N>` | | Number of JMH forks |
| `--warmup <N>` | | Number of warmup iterations |
| `--measurement <N>` | | Number of measurement iterations |
| `--threads <N>` | | Number of threads |
| `--quick` | | Shortcut: 1 fork, 5 warmup, 10 measurement, 10 threads |
| `--output-format <fmt>` | `json` | Result format: `json`, `csv`, `text` |
| `--profiler <list>` | | Comma-separated JMH profilers |
| `--jvm-args "<args>"` | | Extra JVM arguments for benchmark forks |
| `--async-profiler` | | Enable async-profiler CPU profiling |
| `--async-profiler-format <f>` | `flamegraph,jfr` | Async-profiler output: `flamegraph,jfr`, `flamegraph`, or `jfr` |
| `--async-profiler-path <path>` | | Path to async-profiler installation |
| `--cores <spec>` | auto-detect | CPU cores for pinning (e.g., `0-7` or `0,2,4,6`) |
| `-h`, `--help` | | Show help message |

When `--quick` is combined with explicit flags (e.g., `--quick --forks 3`), the explicit flags take precedence.

## Validator Profiles

The `hv-current` profile is used by default. See [README.md](../README.md) for other available profiles.

## JMH Profilers

Use `--profiler` with comma-separated short names:

| Short name | Profiler | What it measures |
|------------|----------|------------------|
| `gc` | GCProfiler | GC allocation rates and pause times |
| `stack` | StackProfiler | Stack-based CPU sampling |
| `cl` | ClassloaderProfiler | Class loading statistics |
| `comp` | CompilerProfiler | JIT compilation statistics |
| `hs_mem` | HotspotMemoryProfiler | Hotspot memory internals |
| `hs_rt` | HotspotRuntimeProfiler | Hotspot runtime counters |
| `hs_thr` | HotspotThreadProfiler | Hotspot thread statistics |
| `hs_comp` | HotspotCompilationProfiler | Hotspot JIT compilation details |

Example: `--profiler gc,stack`

## Async-profiler & Flamegraphs

Enable with `--async-profiler`. By default, the script generates an interactive HTML flamegraph with JIT compilation annotations.
Use `--async-profiler-format jfr` to produce a JFR recording instead (viewable in JDK Mission Control, IntelliJ Profiler, etc.),
or `--async-profiler-format flamegraph,jfr` for both. When both are requested, the profiler records JFR during the run and converts
it to a flamegraph afterwards using the bundled `converter.jar`.

**Resolution order for async-profiler location:**
1. `--async-profiler-path <path>` flag
2. `ASYNC_PROFILER_HOME` environment variable
3. Auto-download to `performance/scripts/.async-profiler/` (gitignored)

The auto-download pins async-profiler to v4.4 and verifies the archive SHA-256 hash before extraction.
Supported platforms: Linux x64, Linux arm64, macOS (universal).

### perf_event_paranoid (Linux only)

For accurate CPU profiling on Linux, `perf_event_paranoid` should be `<= 1`:

```bash
# Check current value
cat /proc/sys/kernel/perf_event_paranoid

# Set for current session
sudo sysctl kernel.perf_event_paranoid=1
```

If the value is too high, async-profiler falls back to itimer mode (less accurate but still functional).

## CPU Core Pinning

On Linux with Intel hybrid CPUs (12th gen+), the script auto-detects P-cores vs E-cores by reading `base_frequency` from sysfs
and pins the benchmark JVM to P-cores only via `taskset`. This avoids inconsistent results from running on efficiency cores.

- **Override:** `--cores 0-7` or `--cores 0,2,4,6`
- **Non-hybrid CPUs:** All frequencies are equal, no pinning applied
- **macOS:** Core pinning is not supported (no `taskset`), skipped with a log message

## Output & Results

Results are written to timestamped directories under `performance/scripts/.results/` (gitignored):

```
performance/scripts/.results/
  hv-current-20260518-001/
    environment.txt          # JVM, CPU, OS, git SHA, benchmark config
    jmh-results.json         # JMH results (format depends on --output-format)
    profile.html             # Async-profiler flamegraph (if enabled, default)
    profile.jfr              # Async-profiler JFR recording (if --async-profiler-format jfr)
  hv-current-20260518-002/
    ...
```

The auto-incremented index (`-001`, `-002`, ...) allows multiple runs without overwrites.
Results are gitignored and persist across `mvn clean`.

## Examples

```bash
# Run only cascaded validation benchmarks
./performance/scripts/run-benchmarks.sh CascadedValidation

# Multiple benchmark patterns
./performance/scripts/run-benchmarks.sh CascadedValidation SimpleValidation

# Full run with GC profiler
./performance/scripts/run-benchmarks.sh --profiler gc

# Quick run with flamegraph
./performance/scripts/run-benchmarks.sh --quick --async-profiler

# Compare two versions
./performance/scripts/run-benchmarks.sh --profile hv-8.0 --quick
./performance/scripts/run-benchmarks.sh --profile hv-current --quick

# Custom thread count and core pinning
./performance/scripts/run-benchmarks.sh --threads 4 --cores 0-3

# Pass extra JVM arguments
./performance/scripts/run-benchmarks.sh --jvm-args "-XX:+UseG1GC -Xmx2g"

# Generate JFR recording instead of flamegraph
./performance/scripts/run-benchmarks.sh --quick --async-profiler --async-profiler-format jfr

# Generate both flamegraph and JFR recording
./performance/scripts/run-benchmarks.sh --quick --async-profiler-format flamegraph,jfr

# Skip rebuild when iterating on profiling
./performance/scripts/run-benchmarks.sh --skip-build --async-profiler SimpleValidation

# Use a local async-profiler installation
./performance/scripts/run-benchmarks.sh --async-profiler-path /opt/async-profiler
```

## Troubleshooting

**Maven build fails for older profiles (hv-4.x, hv-5.x)**
Older Hibernate Validator versions may not compile with modern JDKs. Try building with an older JDK or use `--jvm-args` to set compatibility flags.

**async-profiler SHA-256 mismatch**
Delete `performance/scripts/.async-profiler/` and re-run. If the issue persists, the pinned version's archive
may have changed upstream -- update the hashes in the script.

**Flamegraph is empty or shows only itimer frames**
Check `perf_event_paranoid` (Linux). Set to `<= 1` for hardware CPU event sampling.

**`taskset` not found**
Install `util-linux` package. On macOS, core pinning is not available.

**Permission denied running the script**
```bash
chmod +x performance/scripts/run-benchmarks.sh
```
