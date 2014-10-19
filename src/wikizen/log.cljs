(ns wikizen.log)

(defn ! [& args])

(defn enable-log
  []
  (defn !
    [& args]
    (let [d (js/Date.)]
      (apply println (str (.getHours d) ":"
                          (.getMinutes d) ":"
                          (.getSeconds d) "."
                          (.getMilliseconds d) ":") args))))
(defn error
  [& args]
  (let [d (js/Date.)]
    (.error js/console (apply print-str (str (.getHours d) ":"
                                       (.getMinutes d) ":"
                                       (.getSeconds d) "."
                                       (.getMilliseconds d) ":") args))))