# VimiCalc

**Pre-alpha** — This project is in early development. Expect rough edges, missing features, and breaking changes.

A spreadsheet application with Vim-style modal editing, built with Java and JavaFX.

VimiCalc (also called WeSpreadSheet) combines the functionality of a traditional spreadsheet with Vim's keyboard-driven workflow. Navigate cells with `hjkl`, edit with modal keybindings, write formulas in Reverse Polish Notation, record macros, and more — all without touching the mouse.

## Features

- **Modal editing** — 6 modes inspired by Vim: Normal, Insert, Formula, Command, Visual, and Help
- **Vim-style navigation** — `h/j/k/l` movement, multipliers (`5j`), `g{coords}` go-to (e.g. `gA3`), `Ctrl-O` jump back, and familiar keybindings (`d`, `y`, `p`, `i`, `a`)
- **RPN formulas** — Reverse Polish Notation formula engine with cell references, ranges (`B2:D3`), and relative coordinates (`kl -3j`)
- **Matrix operations** — `matMul`, `det`, `tpose`, `sum`, `prod`, `quot`, and more
- **Math functions** — `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `ln`, `log10`, `logBase`, `exp`, `sqrt`, `abs`, `PI`
- **Macros** — Record with `q`, replay with `@`, chain with multipliers (`5@a`), repeat the last editing command (`dd`, `yy`, `pp`, …) with `.`
- **Conditional execution** — `<formula{then{else}` syntax for conditional key commands
- **Cell merging** — Select cells in Visual mode and press `m`
- **Cell formatting** — Background colors, text colors, and bold via command mode
- **Undo/redo** — `u` to undo, `r` to redo
- **Dependency tracking** — Automatic propagation when referenced cells change
- **File persistence** — Save/load spreadsheets as JSON (`.json`)

## Modes

| Mode | Enter with | Purpose |
|------|-----------|---------|
| **Normal** | `ESC` | Navigation and key commands |
| **Insert** | `i` or `a` | Plain text entry (`ESC` to save, `Shift+ESC` to cancel) |
| **Formula** | `=` | RPN formula entry (confirm with `Enter`, cancel with `ESC`) |
| **Visual** | `v` | Multi-cell selection, merging (`m`), copying (`y`), deleting (`d`), commands (`;`) |
| **Command** | `:` | Vim-style commands (`:w`, `:q`, `:resCol`, etc.) |
| **Help** | `:h`, `:help`, or `:?` | Built-in documentation |

## Commands

| Command | Description |
|---------|-------------|
| `:w [file]` | Save spreadsheet |
| `:e [file]` | Open spreadsheet |
| `:q` / `ZQ` | Quit |
| `:wq` / `ZZ` | Save and quit |
| `:resCol [pixels]` | Resize column width |
| `:resRow [pixels]` | Resize row height |
| `:cellColor [color]` | Set cell background color |
| `:txtColor [color]` | Set text color |
| `:boldTxt` | Toggle bold text |

## Formula Examples

Formulas use Reverse Polish Notation (operands before operators):

```
3 5 +              → 8
B2:D3 F7:G9 matMul → matrix multiplication of two ranges
A1:A10 sum         → sum of a range
kl -3j / 3 mod     → relative cell reference with arithmetic
```

## Requirements

- Java 17+

## Building

The project uses Gradle with the JavaFX plugin — no separate JavaFX SDK install is needed.

```bash
./gradlew build
```

## Testing

Unit and model tests can be run without a display:

```bash
./gradlew test
```

UI tests that boot the JavaFX scene graph require a display. To run all tests on a headless machine (e.g. over SSH without a monitor):

```bash
# Debian / Ubuntu / Termux
sudo apt install xvfb   # or: pkg install xvfb
xvfb-run ./gradlew test
```

The build is preconfigured with `prism.order=sw` so that JavaFX uses software rendering during tests, avoiding GPU dependencies in headless environments.

## Running

```bash
# Launch the application
./gradlew run

# Open a spreadsheet file
./gradlew run --args="mysheet.json"
```

The convenience scripts `wss.sh` (Linux/macOS) and `wss.ps1` (Windows) wrap the Gradle run task.

## Project Structure

```
src/main/java/vimicalc/
├── Main.java              # Entry point
├── controller/            # Input handling, mode switching, key commands
├── model/                 # Cell, Sheet, Formula (RPN engine), Command
├── view/                  # Camera, rendering, UI components, help menu
└── utils/                 # Coordinate conversions, helpers
```

The application follows an MVC architecture, uses a Canvas-based renderer, and stores spreadsheet data as JSON (via Gson).
