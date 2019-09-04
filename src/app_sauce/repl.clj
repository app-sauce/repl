;; Copyright © App Sauce, LLC
;;
;; All rights reserved. This program and the accompanying materials are made
;; available under the terms of the Eclipse Public License v2.0 which
;; accompanies this distribution, and is available at
;; http://www.eclipse.org/legal/epl-v20.html

(ns app-sauce.repl
  "Helpers for managing REPL servers and sessions."
  (:require
    [clojure.java.io :as io]
    [nrepl.server :as nrepl-server]
    [clojure.core.server :as server]

    [rebel-readline.clojure.main :as rebel-main]
    [rebel-readline.core :as rebel-core]))


;; Pluggable start and stop

(defmulti start! (fn [kw] kw))
(defmulti stop!  (fn [kw _] kw))


;; nREPL

(def nrepl-port-file ".nrepl-port")

; Use CIDER's nREPL handler
; https://github.com/clojure-emacs/cider-nrepl
(defn handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(defmethod start! :nrepl
  [type-kw]
  (let [server (nrepl-server/start-server :handler (handler))
        port (:port server)]
    (spit ".nrepl-port" port)
    (println "nREPL server started on port" port)
    server))

(defmethod stop! :nrepl
  [type-kw server]
  (nrepl-server/stop-server server)
  (io/delete-file ".nrepl-port" true))


;; pREPL

(def prepl-name "dev")
(def prepl-port-file ".socket-port")

(defmethod start! :prepl
  [type-kw]
  (let [port (+ 5000 (rand-int 1000))]
    (server/start-server {:accept 'clojure.core.server/io-prepl
                          :address "localhost"
                          :port port
                          :name prepl-name})
    (spit prepl-port-file port)
    (println "pREPL server started on port" port)
    prepl-name))

(defmethod stop! :prepl
  [type-kw server-name]
  (server/stop-server server-name)
  (io/delete-file prepl-port-file))



;; REPL sessions

; TODO: In the future, if we want to support per-REPL config, we can take a map
; instead of a list of IDs.
; Eg. {:nrepl {:port 8042}, :prepl {:name "dev"}}

(defmacro session!
  "Start up the selected REPLs and then execute the body while those REPLs are
  running. Valid REPLs IDs are currently: :nrepl, :prepl."
  [ids & body]
  `(let [started# (doall (map start! ~ids))]
     (flush)
     (try
       (do ~@body)
       (finally
         (doall (map #(stop! %1 %2) ~ids started#))))))


;; Rebel

(defn run-rebel!
  [namespace-symbol]
  (rebel-core/ensure-terminal
    (rebel-main/repl :init (fn [] (in-ns namespace-symbol)))))
