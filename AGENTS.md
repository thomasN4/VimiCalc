# AGENTS.md

Guidance for AI agents working in the VimiCalc codebase. (This file is included by `CLAUDE.md`.)

## What this is

VimiCalc (also called **WeSpreadSheet**) is a desktop spreadsheet application with
Vim-style modal editing, built with **Java 17** and **JavaFX**. Navigation is keyboard-driven
(`hjkl`, multipliers, go-to), formulas are written in **Reverse Polish Notation**, and the app
supports macros, undo/redo, cell merging, formatting, and automatic dependency tracking.

The project is **pre-alpha** — expect rough edges and breaking changes. See `README.md` for the
user-facing feature list (note: the README's "Building/Running" section is accurate, but its claim
that persistence uses `.wss` / Java serialization is **out of date** — see Persistence below).

## Common commands

```bash
./gradlew build                       # compile + run tests
./gradlew test                        # run JUnit 5 tests only (see Testing for UI caveats)
./gradlew run                         # launch the app
./gradlew run --args="myfile.json"    # launch and open a spreadsheet file
```

`wss.sh` (Linux/macOS) and `wss.ps1` (Windows) are thin wrappers around `./gradlew run`.

Running the GUI requires a display. Prefer unit-level verification (or a filtered test run)
over launching the app when you lack a suitable display — see **Testing** below.

## Tech stack

- **Java 17** (Gradle toolchain pins this — don't use newer language features).
- **JavaFX 17.0.7** via the `org.openjfx.javafxplugin` Gradle plugin — no separate SDK install needed.
  Modules in use: `controls`, `fxml`, `media`, `swing`, `web`.
- **Gson 2.11.0** for file persistence.
- **JetBrains annotations** (`@NotNull`, etc.) — `compileOnly`.
- **JUnit 5** (Jupiter 5.10.2) for tests.

## Architecture

MVC, organized under `src/main/java/vimicalc/`:

```
Main.java          # JavaFX Application entry point; loads GUI.fxml, wires keys to Controller
controller/        # Input handling and app state
  Controller.java    # Orchestrates everything: mode, camera, selector, undo/redo, clipboard, macros
  Mode.java          # The 6 editing modes (enum)
  KeyCommand.java    # NORMAL-mode key sequence parsing (hjkl, multipliers, d/y/p, macros, conditionals)
  EditorOperations.java
model/             # Core data model (no JavaFX dependencies on the data itself where avoidable)
  Sheet.java         # The spreadsheet: cell map, dependency graph, formatting, file I/O
  Cell.java          # A single cell: coords, text, numeric value, optional Formula, merge state
  Formula.java       # The RPN formula engine
  Command.java       # Colon-command (`:w`, `:e`, `:resCol`, ...) handling
  Token.java, Tokenizer.java  # Expression/command lexing (symbol vs literal)
  CommandResult.java, FileIOCallbacks.java
view/              # Canvas-based rendering and UI widgets
  Camera.java, CellSelector.java, Positions.java, Formatting.java, Picture.java,
  StatusBar.java, InfoBar.java, HelpMenu.java, CoordsInfo.java, FirstCol/FirstRow, ...
utils/
  Conversions.java   # Coordinate <-> label conversion, number parsing helpers
```

The UI layout lives in `src/main/resources/vimicalc/GUI.fxml`. Rendering is done by drawing onto a
JavaFX `Canvas` via a `GraphicsContext`, not via a scene graph of nodes.

### Key concepts

- **Modes** (`Mode` enum): `NORMAL`, `INSERT`, `FORMULA`, `COMMAND`, `VISUAL`, `HELP`. The
  `Controller` dispatches each `KeyEvent` to a mode-specific handler. When adding behavior, first
  identify which mode it belongs to.
- **Coordinates**: columns and rows are **one-based** (`xCoord` = column, `yCoord` = row). Cells are
  stored in `Sheet` as a `HashMap` keyed by `List.of(x, y)` (see `Sheet.cellKey`). Column labels
  (`A`, `B`, ... `AA`) are produced via `utils/Conversions`.
- **Formulas**: RPN — operands precede operators (`3 5 +` → `8`). Support cell refs, ranges
  (`B2:D3`), relative coords (`kl -3j`), matrix ops, and math functions. The engine is in
  `Formula.java`.
- **Dependency tracking**: `Sheet` maintains a dependency graph so dependent cells re-evaluate when a
  referenced cell changes. **Dependencies are not persisted** — they are rebuilt on load by
  re-evaluating all formula cells.

## Persistence (current reality)

Files are saved as **JSON via Gson**, not Java serialization. `Sheet.writeFile` / `Sheet.readFile`
handle this; the on-disk extension is `.json` (`writeFile` appends `.json` if missing, `readFile`
rejects non-`.json` paths). Saved state covers non-empty cells, non-default formatting, and
column/row size offsets. If you touch the file format, keep write and read in sync and remember that
formula cells are re-evaluated (and dependencies rebuilt) after load.

## Conventions

- **Javadoc** is used heavily on classes, fields, and non-trivial methods. Match this style when
  adding or modifying public API — keep doc comments accurate when you change behavior.
- Use `@NotNull` (JetBrains) where the surrounding code does.
- Stick to Java 17 — no preview/newer features.
- Match the naming and structure of the package you're editing; keep model logic out of view code and
  vice versa.

## Testing

- Unit tests live in `src/test/java/vimicalc/` mirroring the main package layout
  (`model/`, `controller/`, `utils/`, `view/`).
- JUnit 5 with `@Nested`, `@BeforeEach`, and `@TempDir` (for file I/O tests) is the established style.
- There is a **manual** test plan for keyboard interaction at
  `src/test/java/vimicalc/controller/KeyCommandManualTests.md` — interactive behavior that's hard to
  unit-test is verified by hand from a fresh `./gradlew run`. Update it when you change NORMAL-mode key
  handling.
- When adding model/util logic, add or extend the corresponding `*Test.java`.

### Unit vs UI tests

The suite has two kinds of tests; they need different environments:

| Kind | Examples | Where they run |
|------|----------|----------------|
| **Unit** | `model/*Test`, `utils/*Test`, pure `controller` parsing (`KeyCommandParsingTest`, `CommandCompletionTest`) | Anywhere with Java 17 — including Fedora / Wayland |
| **UI (TestFX)** | `AppUiTest`, `ChromeLabelsUiTest`, `HelpMenuUiTest`, `MergeInteractionUiTest`, `ResizeUiTest`, `ViewportSyncUiTest` | Need a real or virtual **X** display |

**Day-to-day verification** (e.g. on Fedora + Wayland, where plain TestFX often fails): run unit tests only — do **not** treat those UI failures as product regressions.

```bash
./gradlew test --tests 'vimicalc.model.*' \
  --tests 'vimicalc.utils.*' \
  --tests 'vimicalc.view.*' \
  --tests 'vimicalc.controller.KeyCommandParsingTest' \
  --tests 'vimicalc.controller.CommandCompletionTest'
```

**Full suite** (including UI tests): use a host with **`xvfb`** installed (e.g. the **kubuntu** box). The build already sets `prism.order=sw` so JavaFX uses software rendering in tests.

```bash
xvfb-run ./gradlew test
```

Install notes and more context live in `README.md` (`sudo apt install xvfb` on Debian/Ubuntu). If the remote checkout looks stale, `./gradlew clean` before testing can avoid odd compile failures from leftover build output.

## Notes for agents

- The repo contains Eclipse project files (`.classpath`, `.project`, `.settings/`) and stray local
  artifacts (e.g. `bin/`, `hs_err_pid*.log`). Don't commit build output or crash logs; respect
  `.gitignore`.
- Confirm before committing or pushing; branch off `main` if you're on it.
- End every PR body and every substantive PR review or comment with an attribution footer naming
  the agent that actually authored it — e.g. `🤖 Generated with [Claude Code](https://claude.com/claude-code)`
  or `🤖 Generated with Grok Build`. Never credit an agent other than the one that did the work.
