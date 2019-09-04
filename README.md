# app-sauce.repl

REPL helpers for Clojure projects that use deps.edn


## Getting Started

Create `dev/repl.clj` in your project with the following contents:

```clojure
(ns repl
  (:require
    [app-sauce.repl :as repl]))

(defn -main [& args]
  (repl/session!
    [:nrepl :prepl]
    (repl/run-rebel! 'user))

  (System/exit 0))
```

Create a `repl` alias in your `deps.edn`:

```clojure
:repl {:main-opts  ["-m" "repl"]
       :extra-paths ["dev" "test"]
       :extra-deps {app-sauce/repl {:mvn/version "0.1.0"}}}
```

Start the REPL with:

```bash
clojure -A:repl
```


## License

Copyright © 2019 App Sauce, LLC

Distributed under the Eclipse Public License.
