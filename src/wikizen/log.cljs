(ns wikizen.log)

(defn !
  "No-op function, used if logging is disabled"
  [& args])

(defn- get-timestamp
  []
  "return the timestamp in the format HH:mm:ss.MMM"
  (let [d (js/Date.)]
    (str (.getHours d) ":"
         (.getMinutes d) ":"
         (.getSeconds d) "."
         (.getMilliseconds d) ":")))

(defn enable-log
  "Enables logging by replacing the no-op function"
  []
  (defn !
    [& args]
    (apply println (get-timestamp) args)))

(defn error
  [& args]
  "Logs an error to console.erro"
  (.error js/console (apply print-str (get-timestamp) args)))