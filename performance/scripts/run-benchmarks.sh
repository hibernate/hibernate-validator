#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
# Copyright Red Hat Inc. and Hibernate Authors
#
# Hibernate Validator JMH benchmark runner with optional async-profiler
# flamegraph support and CPU core pinning.

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PERF_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

readonly ASYNC_PROFILER_VERSION="4.4"
readonly ASYNC_PROFILER_DOWNLOAD_DIR="${SCRIPT_DIR}/.async-profiler"
readonly ASYNC_PROFILER_SHA256_LINUX_X64="1233f26fc95753e75ce32733bbcaf8f0bedc2c098b0e798af87935b08a63b24e"
readonly ASYNC_PROFILER_SHA256_LINUX_ARM64="86ff97b4436accdb6d7bb65c1cf6e38a756f2037a921994d8fa1dcb97d1dc53c"
readonly ASYNC_PROFILER_SHA256_MACOS="6177ebe56d088d116e1b436618f18b3316b9e41885fe743539f6bc297a487239"

readonly VALID_PROFILES=(
    hv-current hv-9.1 hv-9.0 hv-8.0
    hv-6.2 hv-6.1 hv-6.0
    hv-5.4 hv-5.3 hv-5.2 hv-5.1 hv-5.0
    hv-4.3 hv-4.2 hv-4.1
    bval-1.1 bval-3.0
)

readonly VALID_PROFILERS=(gc stack cl comp hs_mem hs_rt hs_thr hs_comp)

readonly VALID_OUTPUT_FORMATS=(json csv text)

readonly VALID_ASYNC_PROFILER_FORMATS=(flamegraph jfr)

# --- Utility functions ---

log() {
    echo "[INFO $(date '+%Y-%m-%d %H:%M:%S')] $*" >&2
}

warn() {
    echo "[WARN $(date '+%Y-%m-%d %H:%M:%S')] $*" >&2
}

die() {
    local msg="$1"
    local code="${2:-1}"
    echo "[ERROR] ${msg}" >&2
    exit "${code}"
}

usage() {
    cat <<'EOF'
Usage: run-benchmarks.sh [OPTIONS] [BENCHMARK_PATTERNS...]

Run Hibernate Validator JMH benchmarks with optional profiling and CPU pinning.

Benchmark patterns are JMH regex patterns matched against fully qualified
benchmark method names. If none are specified, the default benchmark set is run.

Options:
  --profile <name>            Validator profile (default: hv-current)
  --skip-build                Skip Maven build (use existing JAR)
  --forks <N>                 Number of JMH forks
  --warmup <N>                Number of warmup iterations
  --measurement <N>           Number of measurement iterations
  --threads <N>               Number of threads
  --quick                     Quick mode: 1 fork, 5 warmup, 10 measurement, 10 threads
  --output-format <fmt>       Result format: json (default), csv, text
  --profiler <list>           Comma-separated JMH profilers: gc,stack,cl,comp,
                              hs_mem,hs_rt,hs_thr,hs_comp
  --jvm-args "<args>"         Extra JVM arguments passed to benchmark forks
  --async-profiler            Enable async-profiler CPU profiling
  --async-profiler-format <f> Output: flamegraph,jfr (default), flamegraph, or jfr
  --async-profiler-path <p>   Path to async-profiler installation
  --cores <spec>              Pin to specific CPU cores (e.g., 0-7 or 0,2,4,6)
                              Linux only; auto-detects P-cores on hybrid CPUs
  -h, --help                  Show this help message

Environment variables:
  ASYNC_PROFILER_HOME         Path to async-profiler installation (fallback
                              if --async-profiler-path is not specified)

Examples:
  # Run all default benchmarks
  run-benchmarks.sh

  # Quick dev run of a specific benchmark
  run-benchmarks.sh --quick CascadedValidation

  # Full run with GC profiler against HV 9.1
  run-benchmarks.sh --profile hv-9.1 --profiler gc

  # Generate flamegraph
  run-benchmarks.sh --quick --async-profiler SimpleValidation

  # Generate JFR recording instead of flamegraph
  run-benchmarks.sh --quick --async-profiler --async-profiler-format jfr SimpleValidation

  # Custom JVM args and core pinning
  run-benchmarks.sh --jvm-args "-XX:+UseG1GC -Xmx2g" --cores 0-7
EOF
    exit 0
}

# --- Validation helpers ---

is_valid_profile() {
    local profile="$1"
    for p in "${VALID_PROFILES[@]}"; do
        [[ "${p}" == "${profile}" ]] && return 0
    done
    return 1
}

is_positive_int() {
    [[ "$1" =~ ^[1-9][0-9]*$ ]]
}

validate_profilers() {
    local input="$1"
    IFS=',' read -ra items <<< "${input}"
    for item in "${items[@]}"; do
        local found=false
        for vp in "${VALID_PROFILERS[@]}"; do
            if [[ "${vp}" == "${item}" ]]; then
                found=true
                break
            fi
        done
        if [[ "${found}" != true ]]; then
            die "Invalid profiler '${item}'. Valid profilers: ${VALID_PROFILERS[*]}" 1
        fi
    done
}

validate_output_format() {
    local fmt="$1"
    for f in "${VALID_OUTPUT_FORMATS[@]}"; do
        [[ "${f}" == "${fmt}" ]] && return 0
    done
    die "Invalid output format '${fmt}'. Valid formats: ${VALID_OUTPUT_FORMATS[*]}" 1
}

validate_async_profiler_formats() {
    local input="$1"
    IFS=',' read -ra items <<< "${input}"
    for item in "${items[@]}"; do
        local found=false
        for f in "${VALID_ASYNC_PROFILER_FORMATS[@]}"; do
            if [[ "${f}" == "${item}" ]]; then
                found=true
                break
            fi
        done
        if [[ "${found}" != true ]]; then
            die "Invalid async-profiler format '${item}'. Valid formats: ${VALID_ASYNC_PROFILER_FORMATS[*]}" 1
        fi
    done
}

# --- Platform detection ---

detect_platform() {
    local machine
    machine="$(uname -m)"
    case "${machine}" in
        x86_64)  ARCH="x64" ;;
        aarch64|arm64) ARCH="arm64" ;;
        *) die "Unsupported architecture: ${machine}" 2 ;;
    esac

    local kernel
    kernel="$(uname -s)"
    case "${kernel}" in
        Linux)  OS="linux" ;;
        Darwin) OS="macos" ;;
        *) die "Unsupported OS: ${kernel}" 2 ;;
    esac
}

# --- Prerequisites ---

check_prerequisites() {
    command -v java >/dev/null 2>&1 || die "'java' not found on PATH" 2
    command -v mvn >/dev/null 2>&1 || die "'mvn' not found on PATH" 2
}

# --- Async-profiler ---

compute_sha256() {
    local file="$1"
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "${file}" | cut -d' ' -f1
    elif command -v shasum >/dev/null 2>&1; then
        shasum -a 256 "${file}" | cut -d' ' -f1
    else
        die "Neither sha256sum nor shasum found. Cannot verify async-profiler archive." 4
    fi
}

get_expected_sha256() {
    case "${OS}-${ARCH}" in
        linux-x64)  echo "${ASYNC_PROFILER_SHA256_LINUX_X64}" ;;
        linux-arm64) echo "${ASYNC_PROFILER_SHA256_LINUX_ARM64}" ;;
        macos-*)    echo "${ASYNC_PROFILER_SHA256_MACOS}" ;;
        *) die "No SHA-256 hash for platform ${OS}-${ARCH}" 4 ;;
    esac
}

get_async_profiler_archive_name() {
    if [[ "${OS}" == "macos" ]]; then
        echo "async-profiler-${ASYNC_PROFILER_VERSION}-macos.zip"
    else
        echo "async-profiler-${ASYNC_PROFILER_VERSION}-${OS}-${ARCH}.tar.gz"
    fi
}

get_async_profiler_extract_dir() {
    if [[ "${OS}" == "macos" ]]; then
        echo "async-profiler-${ASYNC_PROFILER_VERSION}-macos"
    else
        echo "async-profiler-${ASYNC_PROFILER_VERSION}-${OS}-${ARCH}"
    fi
}

get_profiler_lib_name() {
    if [[ "${OS}" == "macos" ]]; then
        echo "libasyncProfiler.dylib"
    else
        echo "libasyncProfiler.so"
    fi
}

validate_async_profiler_home() {
    local home="$1"
    local lib_name
    lib_name="$(get_profiler_lib_name)"
    if [[ ! -f "${home}/lib/${lib_name}" ]]; then
        die "async-profiler library not found at ${home}/lib/${lib_name}" 4
    fi
}

setup_async_profiler() {
    if [[ -n "${ASYNC_PROFILER_PATH_ARG}" ]]; then
        RESOLVED_ASYNC_PROFILER_HOME="${ASYNC_PROFILER_PATH_ARG}"
        log "Using async-profiler from --async-profiler-path: ${RESOLVED_ASYNC_PROFILER_HOME}"
        validate_async_profiler_home "${RESOLVED_ASYNC_PROFILER_HOME}"
        return
    fi

    if [[ -n "${ASYNC_PROFILER_HOME:-}" ]]; then
        RESOLVED_ASYNC_PROFILER_HOME="${ASYNC_PROFILER_HOME}"
        log "Using async-profiler from ASYNC_PROFILER_HOME: ${RESOLVED_ASYNC_PROFILER_HOME}"
        validate_async_profiler_home "${RESOLVED_ASYNC_PROFILER_HOME}"
        return
    fi

    detect_platform

    local archive_name extract_dir version_file
    archive_name="$(get_async_profiler_archive_name)"
    extract_dir="$(get_async_profiler_extract_dir)"
    version_file="${ASYNC_PROFILER_DOWNLOAD_DIR}/.version"

    RESOLVED_ASYNC_PROFILER_HOME="${ASYNC_PROFILER_DOWNLOAD_DIR}/${extract_dir}"

    if [[ -f "${version_file}" ]] && [[ "$(cat "${version_file}")" == "${ASYNC_PROFILER_VERSION}" ]]; then
        log "Using cached async-profiler ${ASYNC_PROFILER_VERSION} at ${RESOLVED_ASYNC_PROFILER_HOME}"
        validate_async_profiler_home "${RESOLVED_ASYNC_PROFILER_HOME}"
        return
    fi

    log "Downloading async-profiler ${ASYNC_PROFILER_VERSION} for ${OS}-${ARCH}..."
    mkdir -p "${ASYNC_PROFILER_DOWNLOAD_DIR}"

    local download_url="https://github.com/async-profiler/async-profiler/releases/download/v${ASYNC_PROFILER_VERSION}/${archive_name}"
    local tmp_file="${ASYNC_PROFILER_DOWNLOAD_DIR}/${archive_name}"

    curl -fSL -o "${tmp_file}" "${download_url}" \
        || die "Failed to download async-profiler from ${download_url}" 4

    local expected_sha actual_sha
    expected_sha="$(get_expected_sha256)"
    actual_sha="$(compute_sha256 "${tmp_file}")"

    if [[ "${actual_sha}" != "${expected_sha}" ]]; then
        rm -f "${tmp_file}"
        die "SHA-256 mismatch for ${archive_name}. Expected: ${expected_sha}, Got: ${actual_sha}" 4
    fi
    log "SHA-256 verified: ${actual_sha}"

    if [[ "${OS}" == "macos" ]]; then
        unzip -qo "${tmp_file}" -d "${ASYNC_PROFILER_DOWNLOAD_DIR}"
    else
        tar xzf "${tmp_file}" -C "${ASYNC_PROFILER_DOWNLOAD_DIR}"
    fi
    rm -f "${tmp_file}"

    echo "${ASYNC_PROFILER_VERSION}" > "${version_file}"
    log "async-profiler ${ASYNC_PROFILER_VERSION} installed at ${RESOLVED_ASYNC_PROFILER_HOME}"
    validate_async_profiler_home "${RESOLVED_ASYNC_PROFILER_HOME}"
}

ORIGINAL_PERF_EVENT_PARANOID=""

restore_perf_event_paranoid() {
    if [[ -n "${ORIGINAL_PERF_EVENT_PARANOID}" ]]; then
        log "Restoring perf_event_paranoid to ${ORIGINAL_PERF_EVENT_PARANOID}..."
        sudo sysctl -q kernel.perf_event_paranoid="${ORIGINAL_PERF_EVENT_PARANOID}" 2>/dev/null \
            || warn "Failed to restore perf_event_paranoid. Restore manually: sudo sysctl kernel.perf_event_paranoid=${ORIGINAL_PERF_EVENT_PARANOID}"
    fi
}

check_perf_event_paranoid() {
    [[ "${OS}" == "linux" ]] || return 0

    local paranoid_file="/proc/sys/kernel/perf_event_paranoid"
    [[ -f "${paranoid_file}" ]] || return 0

    local val
    val="$(cat "${paranoid_file}")"
    if (( val <= 1 )); then
        return 0
    fi

    warn "perf_event_paranoid is ${val} (needs <= 1 for hardware CPU profiling)."
    echo -n "[PROMPT] Temporarily set perf_event_paranoid=1 for this run? (requires sudo) [y/N] " >&2
    local answer
    read -r answer
    case "${answer}" in
        [yY]|[yY][eE][sS])
            ORIGINAL_PERF_EVENT_PARANOID="${val}"
            trap restore_perf_event_paranoid EXIT INT TERM
            sudo sysctl -q kernel.perf_event_paranoid=1 \
                || die "Failed to set perf_event_paranoid. Try: sudo sysctl kernel.perf_event_paranoid=1" 4
            log "perf_event_paranoid set to 1 (will restore to ${val} on exit)."
            ;;
        *)
            warn "Keeping perf_event_paranoid=${val}. async-profiler will fall back to itimer mode (less accurate)."
            ;;
    esac
}

# --- CPU core pinning ---

detect_cores() {
    if [[ -n "${CORES_OVERRIDE}" ]]; then
        TASKSET_CORES="${CORES_OVERRIDE}"
        log "Using user-specified cores: ${TASKSET_CORES}"
        return
    fi

    if [[ "${OS}" != "linux" ]]; then
        log "CPU core pinning is only supported on Linux. Skipping."
        return
    fi

    if ! command -v taskset >/dev/null 2>&1; then
        warn "'taskset' not found. Skipping CPU core pinning."
        return
    fi

    local freq_dir="/sys/devices/system/cpu"
    if [[ ! -f "${freq_dir}/cpu0/cpufreq/base_frequency" ]]; then
        log "No base_frequency info available. Using all cores (no pinning)."
        return
    fi

    local -A freq_map
    local max_freq=0

    for cpu_dir in "${freq_dir}"/cpu[0-9]*; do
        local cpu_id="${cpu_dir##*cpu}"
        local freq_file="${cpu_dir}/cpufreq/base_frequency"
        [[ -f "${freq_file}" ]] || continue
        local freq
        freq="$(<"${freq_file}")"
        freq_map["${freq}"]+="${cpu_id} "
        if (( freq > max_freq )); then
            max_freq=${freq}
        fi
    done

    if (( ${#freq_map[@]} <= 1 )); then
        log "Uniform CPU frequencies detected. Using all cores (no pinning)."
        return
    fi

    local p_cores="${freq_map[${max_freq}]}"
    TASKSET_CORES="$(echo "${p_cores}" | xargs | tr ' ' ',')"
    TASKSET_CORES="${TASKSET_CORES%,}"
    log "Hybrid CPU detected. Pinning to P-cores (${max_freq} kHz): ${TASKSET_CORES}"
}

# --- Environment logging ---

log_environment() {
    local env_file="${RESULTS_DIR}/environment.txt"

    {
        echo "=== Benchmark Environment ==="
        echo "Date:      $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
        echo "Profile:   ${PROFILE}"
        echo "Git SHA:   $(git -C "${PERF_DIR}" rev-parse --short HEAD 2>/dev/null || echo 'N/A')"
        echo "OS:        $(uname -srm)"
        echo ""

        echo "--- Java ---"
        java -version 2>&1
        echo ""

        echo "--- CPU ---"
        if [[ "${OS}" == "linux" ]]; then
            grep 'model name' /proc/cpuinfo | head -1 | cut -d: -f2 | xargs
            echo "Cores: $(nproc)"
        elif [[ "${OS}" == "macos" ]]; then
            sysctl -n machdep.cpu.brand_string 2>/dev/null || echo "N/A"
            echo "Cores: $(sysctl -n hw.ncpu 2>/dev/null || echo 'N/A')"
        fi

        if [[ -n "${TASKSET_CORES:-}" ]]; then
            echo "Pinned to cores: ${TASKSET_CORES}"
        fi
        echo ""

        echo "--- Benchmark Configuration ---"
        [[ -n "${FORKS}" ]] && echo "Forks:       ${FORKS}"
        [[ -n "${WARMUP}" ]] && echo "Warmup:      ${WARMUP}"
        [[ -n "${MEASUREMENT}" ]] && echo "Measurement: ${MEASUREMENT}"
        [[ -n "${THREADS}" ]] && echo "Threads:     ${THREADS}"
        echo "Format:      ${OUTPUT_FORMAT}"
        [[ -n "${PROFILERS}" ]] && echo "Profilers:   ${PROFILERS}"
        [[ "${ASYNC_PROFILER_ENABLED}" == true ]] && echo "Async-profiler: enabled (${ASYNC_PROFILER_FORMAT})"
        if [[ ${#BENCHMARK_PATTERNS[@]} -gt 0 ]]; then
            echo "Patterns:    ${BENCHMARK_PATTERNS[*]}"
        else
            echo "Patterns:    (defaults)"
        fi
    } | tee "${env_file}" >&2

    echo "" >&2
}

# --- Build ---

build() {
    log "Building benchmark JAR for profile '${PROFILE}'..."
    local build_log
    build_log="$(mktemp)"
    if (cd "${PERF_DIR}" && mvn clean package -Dvalidator="${PROFILE}" -DskipTests) > "${build_log}" 2>&1; then
        rm -f "${build_log}"
    else
        echo "" >&2
        echo "=== Maven build output ===" >&2
        cat "${build_log}" >&2
        echo "=== End of build output ===" >&2
        rm -f "${build_log}"
        die "Maven build failed for profile '${PROFILE}'. Full log above." 3
    fi

    local jar="${PERF_DIR}/target/hibernate-validator-performance-${PROFILE}.jar"
    [[ -f "${jar}" ]] || die "Expected JAR not found: ${jar}" 3
    log "JAR built: ${jar}"
}

# --- Results directory ---

create_results_dir() {
    local base="${SCRIPT_DIR}/.results"
    local date_str
    date_str="$(date +%Y%m%d)"
    local prefix="${PROFILE}-${date_str}"
    local index=1
    while [[ -d "${base}/${prefix}-$(printf '%03d' ${index})" ]]; do
        ((index++))
    done
    RESULTS_DIR="${base}/${prefix}-$(printf '%03d' ${index})"
    mkdir -p "${RESULTS_DIR}"
    log "Results directory: ${RESULTS_DIR}"
}

# --- Run benchmarks ---

map_profiler_name() {
    case "$1" in
        gc)      echo "org.openjdk.jmh.profile.GCProfiler" ;;
        stack)   echo "org.openjdk.jmh.profile.StackProfiler" ;;
        cl)      echo "org.openjdk.jmh.profile.ClassloaderProfiler" ;;
        comp)    echo "org.openjdk.jmh.profile.CompilerProfiler" ;;
        hs_mem)  echo "org.openjdk.jmh.profile.HotspotMemoryProfiler" ;;
        hs_rt)   echo "org.openjdk.jmh.profile.HotspotRuntimeProfiler" ;;
        hs_thr)  echo "org.openjdk.jmh.profile.HotspotThreadProfiler" ;;
        hs_comp) echo "org.openjdk.jmh.profile.HotspotCompilationProfiler" ;;
    esac
}

run_benchmarks() {
    local jar="${PERF_DIR}/target/hibernate-validator-performance-${PROFILE}.jar"
    [[ -f "${jar}" ]] || die "Benchmark JAR not found: ${jar}. Run without --skip-build." 5

    local cmd=()

    if [[ -n "${TASKSET_CORES:-}" ]]; then
        cmd+=(taskset -c "${TASKSET_CORES}")
    fi

    cmd+=(java -jar "${jar}")

    # JMH args for the forked JVMs
    local jvm_args_parts=()
    if [[ -n "${JVM_ARGS}" ]]; then
        jvm_args_parts+=("${JVM_ARGS}")
    fi
    if [[ "${ASYNC_PROFILER_ENABLED}" == true ]]; then
        local lib_name
        lib_name="$(get_profiler_lib_name)"
        local agent_path="${RESOLVED_ASYNC_PROFILER_HOME}/lib/${lib_name}"
        # When both formats are requested, record as JFR and convert to flamegraph after
        if [[ "${ASYNC_PROFILER_FORMAT}" == *"jfr"* ]]; then
            local ap_out="${RESULTS_DIR}/profile.jfr"
        else
            local ap_out="${RESULTS_DIR}/profile.html"
        fi
        jvm_args_parts+=("-agentpath:${agent_path}=start,event=cpu,file=${ap_out},ann")
    fi
    if [[ ${#jvm_args_parts[@]} -gt 0 ]]; then
        cmd+=(-jvmArgs "${jvm_args_parts[*]}")
    fi

    # JMH CLI options
    [[ -n "${FORKS}" ]] && cmd+=(-f "${FORKS}")
    [[ -n "${WARMUP}" ]] && cmd+=(-wi "${WARMUP}")
    [[ -n "${MEASUREMENT}" ]] && cmd+=(-i "${MEASUREMENT}")
    [[ -n "${THREADS}" ]] && cmd+=(-t "${THREADS}")

    local format_ext
    case "${OUTPUT_FORMAT}" in
        json) format_ext="json" ;;
        csv)  format_ext="csv" ;;
        text) format_ext="txt" ;;
    esac
    cmd+=(-rf "${OUTPUT_FORMAT}" -rff "${RESULTS_DIR}/jmh-results.${format_ext}")

    if [[ -n "${PROFILERS}" ]]; then
        IFS=',' read -ra prof_items <<< "${PROFILERS}"
        for p in "${prof_items[@]}"; do
            cmd+=(-prof "$(map_profiler_name "${p}")")
        done
    fi

    for pattern in "${BENCHMARK_PATTERNS[@]}"; do
        cmd+=("${pattern}")
    done

    log "Executing: ${cmd[*]}"
    echo "" >&2

    "${cmd[@]}" || die "Benchmark execution failed" 5

    echo "" >&2

    if [[ "${ASYNC_PROFILER_ENABLED}" == true ]]; then
        # When both formats requested, convert JFR → flamegraph HTML
        if [[ "${ASYNC_PROFILER_FORMAT}" == *"flamegraph"* ]] && [[ "${ASYNC_PROFILER_FORMAT}" == *"jfr"* ]]; then
            log "Converting JFR recording to flamegraph..."
            local jfrconv="${RESOLVED_ASYNC_PROFILER_HOME}/bin/jfrconv"
            if [[ -x "${jfrconv}" ]]; then
                "${jfrconv}" --cpu --lines "${RESULTS_DIR}/profile.jfr" "${RESULTS_DIR}/profile.html" \
                    || warn "Failed to convert JFR to flamegraph. JFR recording is still available."
            else
                warn "jfrconv not found at ${jfrconv}. Skipping flamegraph conversion."
            fi
        fi

        IFS=',' read -ra ap_fmts <<< "${ASYNC_PROFILER_FORMAT}"
        for fmt in "${ap_fmts[@]}"; do
            case "${fmt}" in
                flamegraph) log "Flamegraph: ${RESULTS_DIR}/profile.html" ;;
                jfr)        log "JFR recording: ${RESULTS_DIR}/profile.jfr" ;;
            esac
        done
    fi

    log "Results written to ${RESULTS_DIR}/"
}

# --- Main ---

main() {
    local PROFILE="hv-current"
    local SKIP_BUILD=false
    local FORKS=""
    local WARMUP=""
    local MEASUREMENT=""
    local THREADS=""
    local QUICK=false
    local OUTPUT_FORMAT="json"
    local PROFILERS=""
    local JVM_ARGS=""
    local ASYNC_PROFILER_ENABLED=false
    local ASYNC_PROFILER_FORMAT="flamegraph,jfr"
    local ASYNC_PROFILER_PATH_ARG=""
    local CORES_OVERRIDE=""
    local BENCHMARK_PATTERNS=()
    local TASKSET_CORES=""
    local RESULTS_DIR=""
    local RESOLVED_ASYNC_PROFILER_HOME=""
    local OS=""
    local ARCH=""

    while [[ $# -gt 0 ]]; do
        case "$1" in
            --profile)
                [[ -n "${2:-}" ]] || die "--profile requires a value"
                PROFILE="$2"; shift 2 ;;
            --skip-build)
                SKIP_BUILD=true; shift ;;
            --forks)
                [[ -n "${2:-}" ]] || die "--forks requires a value"
                is_positive_int "$2" || die "--forks must be a positive integer"
                FORKS="$2"; shift 2 ;;
            --warmup)
                [[ -n "${2:-}" ]] || die "--warmup requires a value"
                is_positive_int "$2" || die "--warmup must be a positive integer"
                WARMUP="$2"; shift 2 ;;
            --measurement)
                [[ -n "${2:-}" ]] || die "--measurement requires a value"
                is_positive_int "$2" || die "--measurement must be a positive integer"
                MEASUREMENT="$2"; shift 2 ;;
            --threads)
                [[ -n "${2:-}" ]] || die "--threads requires a value"
                is_positive_int "$2" || die "--threads must be a positive integer"
                THREADS="$2"; shift 2 ;;
            --quick)
                QUICK=true; shift ;;
            --output-format)
                [[ -n "${2:-}" ]] || die "--output-format requires a value"
                OUTPUT_FORMAT="$2"; shift 2 ;;
            --profiler)
                [[ -n "${2:-}" ]] || die "--profiler requires a value"
                PROFILERS="$2"; shift 2 ;;
            --jvm-args)
                [[ -n "${2:-}" ]] || die "--jvm-args requires a value"
                JVM_ARGS="$2"; shift 2 ;;
            --async-profiler)
                ASYNC_PROFILER_ENABLED=true; shift ;;
            --async-profiler-format)
                [[ -n "${2:-}" ]] || die "--async-profiler-format requires a value"
                ASYNC_PROFILER_FORMAT="$2"
                ASYNC_PROFILER_ENABLED=true; shift 2 ;;
            --async-profiler-path)
                [[ -n "${2:-}" ]] || die "--async-profiler-path requires a value"
                ASYNC_PROFILER_ENABLED=true
                ASYNC_PROFILER_PATH_ARG="$2"; shift 2 ;;
            --cores)
                [[ -n "${2:-}" ]] || die "--cores requires a value"
                CORES_OVERRIDE="$2"; shift 2 ;;
            -h|--help)
                usage ;;
            --)
                shift; BENCHMARK_PATTERNS+=("$@"); break ;;
            -*)
                die "Unknown option: $1" ;;
            *)
                BENCHMARK_PATTERNS+=("$1"); shift ;;
        esac
    done

    # Apply --quick defaults (explicit flags take precedence)
    if [[ "${QUICK}" == true ]]; then
        [[ -z "${FORKS}" ]] && FORKS=1
        [[ -z "${WARMUP}" ]] && WARMUP=5
        [[ -z "${MEASUREMENT}" ]] && MEASUREMENT=10
        [[ -z "${THREADS}" ]] && THREADS=10
    fi

    # Validate
    is_valid_profile "${PROFILE}" || die "Invalid profile '${PROFILE}'. Valid profiles: ${VALID_PROFILES[*]}"
    validate_output_format "${OUTPUT_FORMAT}"
    [[ -n "${PROFILERS}" ]] && validate_profilers "${PROFILERS}"
    [[ "${ASYNC_PROFILER_ENABLED}" == true ]] && validate_async_profiler_formats "${ASYNC_PROFILER_FORMAT}"

    detect_platform
    check_prerequisites

    if [[ "${ASYNC_PROFILER_ENABLED}" == true ]]; then
        setup_async_profiler
        check_perf_event_paranoid
    fi

    detect_cores

    if [[ "${SKIP_BUILD}" != true ]]; then
        build
    fi

    create_results_dir
    log_environment
    run_benchmarks
}

main "$@"
