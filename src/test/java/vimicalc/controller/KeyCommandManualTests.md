# KeyCommand Manual Test Plan

Run the app with `./gradlew run` and perform each test from a fresh launch
(or press `u` repeatedly to undo back to a clean state).

Legend:
- Keys in `backticks` are pressed sequentially, one at a time
- "cell A1" means column A, row 1 (top-left)
- "info bar" is the bar at the bottom showing cell content / messages

---

## 1. Movement (h, j, k, l)

### 1.1 Basic movement
1. Starting at A1, press `l` — cursor moves to B1
2. Press `j` — cursor moves to B2
3. Press `h` — cursor moves to A2
4. Press `k` — cursor moves to A1

### 1.2 Movement with multiplier
1. Starting at A1, press `5` `l` — cursor moves to F1
2. Press `3` `j` — cursor moves to F4
3. Press `1` `2` `h` — cursor should stop at the left edge (A4), not go negative

### 1.3 Arrow keys
1. Starting at A1, press Right arrow — same as `l`
2. Press Down arrow — same as `j`
3. Press Up arrow — same as `k`
4. Press Left arrow — same as `h`

---

## 2. Cell Editing (i, a, =)

### 2.1 Insert mode
1. At A1, press `i` — mode changes to INSERT
2. Type "hello" then press Escape — cell shows "hello", back to NORMAL

### 2.2 Formula mode (RPN syntax)
1. At B1, press `=` — mode changes to FORMULA
2. Type "2 3 +" then press Enter — cell shows 5 (RPN: operands before operator)

---

## 3. Delete (dd)

### 3.1 Delete a cell
1. Enter "test" into A1 (press `i`, type "test", Escape)
2. Press `d` `d` — cell A1 is cleared, info bar updates

### 3.2 Delete empty cell
1. Navigate to an empty cell
2. Press `d` `d` — info bar shows "CAN'T DELETE RIGHT NOW"

### 3.3 Range delete (d{n}{dir})
1. Enter values in A1 through A4 ("a", "b", "c", "d")
2. Navigate to A1
3. Press `d` `3` `j` — all 4 cells (A1-A4) should be deleted
4. Undo with `u` repeatedly to restore

### 3.4 Range delete horizontal
1. Enter values in A1, B1, C1
2. Navigate to A1
3. Press `d` `2` `l` — A1, B1, C1 should all be deleted

---

## 4. Yank and Paste (yy, yd, pp)

### 4.1 Copy and paste a text cell
1. Enter "hello" into A1
2. At A1, press `y` `y` — cell is yanked (no visible change)
3. Navigate to B1, press `p` `p` — B1 now shows "hello"

### 4.2 Copy and paste a formula cell
1. Enter formula into A1 (press `=`, type "2 3 +", Enter) — cell shows 5, info bar shows `(=5.0) (f: 2 3 +)`
2. At A1, press `y` `y`
3. Navigate to B1, press `p` `p` — B1 should show 5, info bar shows `(=5.0) (f: 2 3 +)`

### 4.3 Cut (yank + delete)
1. Enter "cutme" into A1
2. At A1, press `y` `d` — cell is yanked AND deleted (A1 is now empty)
3. Navigate to C1, press `p` `p` — C1 shows "cutme"

### 4.4 Copy empty cell
1. Navigate to an empty cell
2. Press `y` `y` — info bar shows "CAN'T COPY, CELL IS EMPTY"

### 4.5 Paste with nothing copied
1. Fresh launch (nothing yanked yet)
2. Press `p` `p` — info bar shows "CAN'T PASTE, NOTHING HAS BEEN COPIED YET"

### 4.6 Multi-cell copy/paste (VISUAL mode)
1. Enter "A" in A1, "B" in A2, "C" in A3
2. Navigate to A1, press `v` to enter VISUAL mode
3. Press `j` `j` to extend selection to A3
4. Press `y` to yank the selection
5. Navigate to B1, press `p` `p`
6. B1 should show "A", B2 "B", B3 "C" (shape preserved)

---

## 5. Undo / Redo (u, r)

### 5.1 Basic undo
1. Enter "first" in A1
2. Press `u` — A1 should revert to empty

### 5.2 Redo
1. After undoing (5.1), press `r` — A1 shows "first" again

### 5.3 Undo at limit
1. Fresh launch, press `u` — info bar shows "Already at earliest change."

### 5.4 Redo at limit
1. Fresh launch, press `r` — info bar shows "Already at latest change."

---

## 6. Go-to (g{coords})

### 6.1 Go to a cell
1. Press `g` `A` `3` — nothing happens yet (waiting for a trailing key)
2. Press `h` — cursor jumps to A3, then the `h` executes (moves left to... stays at column A)
3. Alternatively: `g` `C` `5` `l` — jumps to C5 then moves right to D5

### 6.2 Go to and jump back (Ctrl-O)
1. At A1, press `g` `C` `5` `l` — cursor jumps to C5 then moves to D5
2. Press Ctrl-O — cursor jumps back to A1

---

## 7. Macros (q{letter}, @{letter}, .)

### 7.1 Record and play a macro
1. Press `q` `a` — info bar shows "Recording macro 'a' ..."
2. Press `i`, type "macro", press Escape, press `j`
3. Press `q` — info bar shows "Macro recorded"
4. Press `@` `a` — the cell below should now contain "macro", cursor moves down

### 7.2 Repeat last editing command (.)
Note: `.` replays the last *editing* expression (`dd`, `yy`, `yd`, `pp`, `m`,
macro replays). Movement keys, undo/redo, and mode switches do not overwrite
it — Vim-like behavior, per issue #24.

1. Enter "one" into A1 and "two" into A2
2. At A1, press `d` `d` — A1 is deleted
3. Press `j` — moves to A2
4. Press `.` — A2 is deleted (replays `dd`, not `j`)

### 7.3 Dot ignores movement-only presses
1. At A1, press `3` `j` — moves to A4
2. Press `.` — nothing moves (movement never enters the dot register;
   if an edit was made earlier in the session, that edit repeats instead)

### 7.4 Paced playback (:macroDelay)
1. Record a macro: `q` `a`, then `i`, type "x", Escape, `j`, then `q`
2. Press `;` and run `macroDelay 200`
3. Press `3` `@` `a` — the three cells fill in visibly one step at a time
   (about one keystroke every 200ms), not all at once
4. While it plays, press `l` or type letters — they are ignored (the cursor
   only moves as the macro dictates)
5. Press `;` and run `macroDelay` (no argument), then `3` `@` `a` — the
   replay is instant again

### 7.5 Abort paced playback with ESC
1. With the macro from 7.4 recorded, run `macroDelay 500`
2. Press `9` `@` `a`, then press Escape mid-replay — playback stops,
   info bar shows "Macro playback aborted.", remaining steps never run
3. Press `j` — normal input works again immediately

### 7.6 Recording is not polluted by paced replay
1. Run `macroDelay 200`
2. Record `q` `b`, press `@` `a`, wait for it to finish, press `l`, press `q`
3. Press `@` `b` — it performs macro a's steps then moves right, i.e. the
   replayed events were recorded once as `@a` (not expanded), and nothing
   was double-recorded

### 7.7 Recording indicator chip
1. Press `j`, `l`, `v`, Escape — the header-corner chip never appears and
   never shows a key name
2. Record a small macro into register `x` (as in 7.1) for use in step 5
3. Press `q` `a` — a red chip showing "● a" appears over the header corner
4. Press `i`, type "text", press Escape, press `j`, press `v`, press Escape —
   the chip stays visible with "● a" through every mode change
5. Press `@` `x` while still recording — the chip does not flicker or hide
   during the replay
6. Press `q` — info bar shows "Macro recorded" and the chip disappears

---

## 8. Quit shortcuts (ZZ, ZQ)

### 8.1 Save and quit
1. Press `Z` `Z` — app saves and exits

### 8.2 Quit without saving
1. Press `Z` `Q` — app exits without saving

### 8.3 Invalid Z command
1. Press `Z` `x` — should reset expression, nothing happens

---

## 9. Range paste (p{n}{dir})

### 9.1 Paste across a range
1. Enter "val" into A1, yank it with `y` `y`
2. Navigate to B1
3. Press `p` `3` `j` — B1, B2, B3, B4 should all contain "val"

---

## 10. Invalid / Incomplete Commands (Expression Reset)

### 10.1 Invalid second char for yank
1. Press `y` `x` — info bar shows "Invalid command: yx", expression resets
2. You can immediately type a new command

### 10.2 Invalid second char for delete
1. Press `d` `x` — info bar shows "Invalid command: dx", expression resets
   (note: `d` + movement key is a valid range-op, e.g. `d` `l` deletes rightward)

### 10.3 Invalid second char for paste
1. Press `p` `x` — info bar shows "Invalid command: px", expression resets
   (note: `p` + movement key is a valid range-op, e.g. `p` `j` pastes downward)

### 10.4 Ctrl-C to cancel partial input
1. Type `y` (expression shows "y" in info bar, waiting for second char)
2. Press Ctrl-C — expression clears

### 10.5 Entering COMMAND mode to escape
1. Type `y` (expression waiting for second char)
2. Press `;` — enters COMMAND mode, expression resets

---

## 11. Zoom (Ctrl+=, Ctrl+-, Ctrl+0, :zoom)

### 11.1 Zoom in / out / reset with keys
1. Press Ctrl+`=` three times — cells, their text, and the row/column header
   labels all grow together; the status/info bars stay their normal size;
   info bar shows "Zoom: 133%"
2. Press Ctrl+`-` once — one step back down, info bar shows "Zoom: 121%"
3. Press Ctrl+`0` — layout returns exactly to the original size,
   info bar shows "Zoom: 100%"

### 11.2 Un-modified keys keep their meaning
1. Press `=` (no Ctrl) — enters FORMULA mode as usual; press Escape
2. Press `1` `0` `j` — cursor moves down 10 cells (the `0` still works
   as a multiplier digit); the view does not zoom

### 11.3 :zoom command
1. Press `;` then type `zoom 200` and press Enter — cells double in size
2. Press `;` then type `zoom` (no argument) and press Enter — back to 100%
3. Press `;` then type `zoom 10` and press Enter — info bar shows the error
   "zoom expects a percentage between 25 and 400."

### 11.4 Zoom interacts with scroll and resized cells
1. Navigate far from the origin (e.g. `g` T40) so the view is scrolled
2. Press Ctrl+`=` a few times — the cursor stays visible (the view
   auto-scrolls if the zoomed cell would fall off the edge)
3. Press Ctrl+`0`, then on some column run `;` `resCol 40` Enter
4. Press Ctrl+`=` — the widened column grows proportionally
   (stays wider than its neighbors); Ctrl+`0` restores it to exactly
   default + 40 pixels

### 11.5 Zoom with merged cells
1. Select a block in VISUAL mode (`v`, move, `m`) to merge it
2. Press Ctrl+`=` — the merged block scales as one region and its
   text scales with it

## 12. Command-name completion (TAB in COMMAND mode)

### 12.1 Cycle through matches
1. Press `;` then type `res` and press Tab — command line reads `:resCol`,
   the right side of the info bar shows `[resCol]  resRow`
2. Press Tab again — command line reads `:resRow`, brackets move to `resRow`
3. Press Tab again — the typed prefix `res` is restored
4. Press Shift+Tab — cycles backwards (to `resRow`)

### 12.2 Session ends on other keys
1. Press `;`, type `font`, press Tab (→ `fontSize`), then type ` 18`
   and press Enter — the current cell's font size changes; the candidate
   list disappears
2. Press `;`, type `x`, press Tab — the right side of the info bar shows
   "No matching command", command line keeps `:x`
3. Press `;`, press Tab with nothing typed — cycles through every command;
   press Escape — back to NORMAL mode with no command run

### 12.3 No completion for VISUAL-mode formulas or arguments
1. Select cells in VISUAL mode, press `;`, type a formula and press Tab —
   nothing is completed and no tab character is inserted
2. Press `;`, type `cellColor r`, press Tab — nothing changes (completion
   only applies before the first space)
