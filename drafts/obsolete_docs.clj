(ns obsolete-docs)

;; ### VSCode Calva
;; **(OBSOLETE, will be fixed soon)**
;;
;; Please add the following command to your [`keybindings.json` file](https://code.visualstudio.com/docs/getstarted/keybindings#_advanced-customization) at the VScode setup (you may pick another key, of course). This command would evaluate a piece of code and send the result to be visualized in Clay.

;; ```json
;; {
;;  "key": "ctrl+shift+enter",
;;  "command": "calva.runCustomREPLCommand",
;;  "args": "(scicloj.clay.v2.api/handle-form! (quote $current-form))"
;;  }
;; ```

;; ### IntelliJ Cursivei
;; **(OBSOLETE, will be fixed soon)**
;;
;; Under preferences, search for "REPL Commands"
;; (or use the menu IntelliJ -> Preferences -> Languages and Frameworks -> Clojure -> REPL Commands)
;;
;; Then add a global command, and edit it with these settings:
;;
;; |  |  |
;; |--|--|
;; | Name: | Send top-level to Clay |
;; | Before Execution: | "Do nothing" |
;; | Execution: | Command `(scicloj.clay.v2.api/handle-form! (quote ~top-level-form))` |
;; | Echo to REPL: | Executed form |
;; | Execution namespace: | Current REPL namespace |
;;
;; It is useful to add 3 commands:
;;
;; * `(scicloj.clay.v2.api/handle-form! (quote ~top-level-form))`
;; * `(scicloj.clay.v2.api/handle-form! (quote ~form-before-caret))`
;; * `(scicloj.clay.v2.api/show-namespace! "~file-path")`
;;
;; You can then add keybindings under Preferences -> Keymap for the new commands.
;;
;; See the Cursive documentation on [REPL commands and substitutions](https://cursive-ide.com/userguide/repl.html#repl-commands) for more details.
;;
